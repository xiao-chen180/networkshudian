package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 找书助手（本地语义打分版）。
 *
 * 当前不依赖外部大模型：把自然语言查询拆成关键词，再按「主题意图 → 分类/关键词」映射，
 * 对书名、作者、分类、简介做加权打分，返回带推荐理由的书单。
 * 若后续接入 LLM，只需把 {@link #search} 中的打分结果作为检索上下文交给模型生成 reply。
 */
@Service
public class BookAssistantService {

    /** 常见意图词 → 主题关键词（命中后用于加权） */
    private static final Map<String, String[]> INTENTS = new LinkedHashMap<>();

    static {
        INTENTS.put("安静", new String[]{"散文", "随笔", "治愈", "温暖", "慢"});
        INTENTS.put("治愈", new String[]{"治愈", "温暖", "散文", "随笔"});
        INTENTS.put("放松", new String[]{"散文", "随笔", "轻松", "温暖"});
        INTENTS.put("想哭", new String[]{"悲剧", "爱情", "命运", "离别", "经典"});
        INTENTS.put("悲伤", new String[]{"悲剧", "离别", "命运"});
        INTENTS.put("悬疑", new String[]{"悬疑", "推理", "犯罪", "案件"});
        INTENTS.put("推理", new String[]{"推理", "悬疑", "侦探"});
        INTENTS.put("烧脑", new String[]{"悬疑", "推理", "科幻"});
        INTENTS.put("科幻", new String[]{"科幻", "宇宙", "未来", "星"});
        INTENTS.put("宇宙", new String[]{"科幻", "宇宙", "星"});
        INTENTS.put("历史", new String[]{"历史", "古代", "传记", "王朝"});
        INTENTS.put("传记", new String[]{"传记", "人物", "历史"});
        INTENTS.put("编程", new String[]{"编程", "计算机", "代码", "程序", "开发"});
        INTENTS.put("入门", new String[]{"入门", "基础", "教程", "初学"});
        INTENTS.put("算法", new String[]{"算法", "数据结构", "计算机"});
        INTENTS.put("经济", new String[]{"经济", "金融", "商业", "市场"});
        INTENTS.put("管理", new String[]{"管理", "组织", "商业", "领导"});
        INTENTS.put("职场", new String[]{"职场", "管理", "效率", "沟通"});
        INTENTS.put("哲学", new String[]{"哲学", "思想", "存在", "人生"});
        INTENTS.put("思考", new String[]{"哲学", "思想", "认知", "思维"});
        INTENTS.put("人生", new String[]{"人生", "命运", "存在", "哲学"});
        INTENTS.put("成长", new String[]{"成长", "青春", "自我", "人生"});
        INTENTS.put("爱情", new String[]{"爱情", "感情", "恋", "婚"});
        INTENTS.put("文学", new String[]{"小说", "文学", "散文", "诗"});
        INTENTS.put("名著", new String[]{"经典", "名著", "文学"});
        INTENTS.put("小孩", new String[]{"儿童", "童", "绘本", "教育"});
        INTENTS.put("考试", new String[]{"考试", "教材", "教育", "学习"});
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "我想", "想找", "一本", "一本关于", "有没有", "推荐", "关于", "什么", "好看", "的书", "可以", "适合", "比较", "一些", "帮我", "给我");

    private static final String NOISE_PREFIX =
            "的了是在我你他她它想找一有和就都也很能看点什么怎样与及或者把被让给对从到为以于还又再才更最";

    private final BookRepository bookRepository;

    public BookAssistantService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * @param query 自然语言查询，例如「想找一本能安静下来的散文」
     * @return reply（话术）、keywords（识别到的关键词）、theme（命中的主题）、results（推荐书 + 理由）
     */
    public Map<String, Object> search(String query, int limit) {
        String q = query == null ? "" : query.trim();
        List<String> keywords = extractKeywords(q);
        Set<String> themes = new LinkedHashSet<>();
        Set<String> intentWords = new LinkedHashSet<>();
        for (String kw : keywords) {
            for (Map.Entry<String, String[]> e : INTENTS.entrySet()) {
                if (kw.contains(e.getKey()) || e.getKey().contains(kw)) {
                    themes.add(e.getKey());
                    intentWords.addAll(Arrays.asList(e.getValue()));
                }
            }
        }

        List<Book> books = bookRepository.findAll();
        List<ScoredBook> scored = new ArrayList<>();
        for (Book book : books) {
            double score = scoreBook(book, keywords, intentWords);
            if (score > 0) {
                boolean intentHit = intentWords.stream().anyMatch(word -> matchesIntent(book, word));
                scored.add(new ScoredBook(book, score, reasonOf(book, keywords, intentWords), intentHit));
            }
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        int size = Math.max(1, Math.min(limit, 12));
        List<Map<String, Object>> results = scored.stream()
                .filter(sb -> themes.isEmpty() || sb.intentHit)
                .limit(size)
                .map(sb -> toItem(sb.book, sb.reason))
                .collect(Collectors.toList());

        // 命中太少时，补充评分高的同类书，避免结果过少
        if (results.size() < 3) {
            Set<Long> picked = scored.stream().limit(size).map(sb -> sb.book().getId()).collect(Collectors.toSet());
            List<Map<String, Object>> extra = books.stream()
                    .filter(b -> !picked.contains(b.getId()))
                    .sorted(Comparator.comparing((Book b) -> b.getAvgRating() == null ? 0d : b.getAvgRating().doubleValue()).reversed())
                    .limit(3 - results.size())
                    .map(b -> toItem(b, "同店高评分，也许你会喜欢"))
                    .collect(Collectors.toList());
            results.addAll(extra);
        }

        // 关键词太泛或完全没命中时，退化为高分好评书
        if (results.isEmpty()) {
            results = books.stream()
                    .sorted(Comparator.comparing((Book b) -> b.getAvgRating() == null ? 0d : b.getAvgRating().doubleValue()).reversed()
                            .thenComparing(b -> b.getSalesCount() == null ? 0 : -b.getSalesCount()))
                    .limit(size)
                    .map(b -> toItem(b, "暂无直接匹配，先按评分和销量推荐给你"))
                    .collect(Collectors.toList());
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("reply", buildReply(q, keywords, themes, results.size()));
        resp.put("keywords", keywords);
        resp.put("themes", themes);
        resp.put("results", results);
        return resp;
    }

    private List<String> extractKeywords(String q) {
        String clean = q.replaceAll("[，。！？、,.!?;；:：\"'“”‘’()（）]", " ");
        List<String> raw = Arrays.stream(clean.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 中文没有空格时，整句作为一个 token，再补充 2 字片段提升召回
        List<String> out = new ArrayList<>();
        for (String token : raw) {
            String t = token;
            for (String stop : STOP_WORDS) {
                t = t.replace(stop, " ");
            }
            for (String part : t.split("\\s+")) {
                if (part.length() < 2) continue;
                out.add(part);
                if (part.length() >= 4) {
                    for (int i = 0; i + 2 <= part.length(); i++) {
                        String bigram = part.substring(i, i + 2);
                        if (NOISE_PREFIX.indexOf(bigram.charAt(0)) < 0) {
                            out.add(bigram);
                        }
                    }
                }
            }
        }
        return out.stream().distinct().limit(12).collect(Collectors.toList());
    }

    private double scoreBook(Book book, List<String> keywords, Set<String> intentWords) {
        String name = lower(book.getName());
        String author = lower(book.getAuthor());
        String summary = lower(book.getSummary());
        String catalog = lower(book.getCatalog());
        String category = book.getCategory() != null ? lower(book.getCategory().getName()) : "";

        double score = 0;
        for (String kw : keywords) {
            String k = lower(kw);
            if (name.contains(k)) score += 12;
            if (author.contains(k)) score += 8;
            if (category.contains(k)) score += 6;
            if (summary != null && summary.contains(k)) score += 3;
            if (catalog != null && catalog.contains(k)) score += 1;
        }
        for (String word : intentWords) {
            if (word.length() < 2) continue;
            if (name.contains(word)) score += 5;
            if (category.contains(word)) score += 5;
            if (summary != null && summary.contains(word)) score += 2;
        }
        if (score > 0) {
            // 口碑与销量做温和加权，避免低质书排前面
            double rating = book.getAvgRating() == null ? 0 : book.getAvgRating().doubleValue();
            int sales = book.getSalesCount() == null ? 0 : book.getSalesCount();
            score += Math.min(rating, 5) * 0.8 + Math.min(sales, 200) * 0.01;
        }
        return score;
    }

    private String reasonOf(Book book, List<String> keywords, Set<String> intentWords) {
        String name = lower(book.getName());
        String category = book.getCategory() != null ? lower(book.getCategory().getName()) : "";
        String summary = lower(book.getSummary());

        for (String kw : keywords) {
            String k = lower(kw);
            if (name.contains(k)) return "书名包含「" + k + "」";
            if (category.contains(k)) return "属于「" + book.getCategory().getName() + "」类";
            if (summary != null && summary.contains(k)) return "简介提到「" + k + "」";
        }
        for (String word : intentWords) {
            if (name.contains(word) || category.contains(word) || (summary != null && summary.contains(word))) {
                return "契合你想要的「" + word + "」主题";
            }
        }
        double rating = book.getAvgRating() == null ? 0 : book.getAvgRating().doubleValue();
        return rating >= 4 ? "口碑不错（评分 " + rating + "）" : "与你描述相关";
    }

    private Map<String, Object> toItem(Book book, String reason) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", book.getId());
        item.put("name", book.getName());
        item.put("author", book.getAuthor());
        item.put("price", book.getPrice());
        item.put("imageUrl", book.getImageUrl());
        item.put("avgRating", book.getAvgRating());
        item.put("categoryName", book.getCategory() != null ? book.getCategory().getName() : "");
        item.put("stock", book.getStock());
        item.put("reason", reason);
        return item;
    }

    private String buildReply(String query, List<String> keywords, Set<String> themes, int count) {
        if (themes.isEmpty()) {
            List<String> display = keywords.stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .limit(3)
                    .collect(Collectors.toList());
            if (display.isEmpty()) display = List.of(query);
            return "我按「" + String.join("、", display) + "」帮你找了 " + count + " 本，点开看看是不是你想找的。";
        }
        return "听上去你想要「" + String.join("、", themes) + "」方向的书，我挑了 " + count + " 本，理由写在每本书下面。";
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private boolean matchesIntent(Book book, String word) {
        if (word == null || word.length() < 2) return false;
        String w = lower(word);
        String name = lower(book.getName());
        String category = book.getCategory() != null ? lower(book.getCategory().getName()) : "";
        String summary = lower(book.getSummary());
        return name.contains(w) || category.contains(w) || summary.contains(w);
    }

    private record ScoredBook(Book book, double score, String reason, boolean intentHit) {}
}