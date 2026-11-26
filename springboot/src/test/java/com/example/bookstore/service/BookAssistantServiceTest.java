package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AI 找书助手的检索质量测试：验证意图映射、排序与兜底策略。
 */
class BookAssistantServiceTest {

    private BookRepository bookRepository;
    private BookAssistantService service;

    private Book book(long id, String name, String author, String summary, String category) {
        Category c = new Category();
        c.setId(id * 10);
        c.setName(category);

        Book b = new Book();
        b.setId(id);
        b.setName(name);
        b.setAuthor(author);
        b.setSummary(summary);
        b.setCategory(c);
        b.setPrice(new BigDecimal("49.00"));
        b.setStock(10);
        b.setAvgRating(new BigDecimal("4.5"));
        b.setSalesCount(100);
        return b;
    }

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        service = new BookAssistantService(bookRepository);
        when(bookRepository.findAll()).thenReturn(List.of(
                book(1, "Python编程：从入门到实践", "Eric Matthes", "零基础学 Python 的编程入门教程", "科技计算机"),
                book(2, "深入理解计算机系统", "Randal Bryant", "计算机系统与底层原理", "科技计算机"),
                book(3, "三体", "刘慈欣", "雨果奖获奖科幻巨著，讲述宇宙文明与未来", "文学小说"),
                book(4, "红楼梦", "曹雪芹", "中国古典四大名著之首，描写爱情与家族命运", "文学小说"),
                book(5, "撒哈拉的故事", "三毛", "三毛的散文集，记录沙漠生活的温暖片段", "文学小说"),
                book(6, "毛泽东传", "罗斯·特里尔", "历史人物传记，讲述近现代历史", "历史传记")
        ));
    }

    @Test
    @DisplayName("编程入门类提问应优先返回计算机类图书")
    @SuppressWarnings("unchecked")
    void shouldPrioritizeProgrammingBooks() {
        Map<String, Object> resp = service.search("编程入门看什么好", 4);

        List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).get("name")).isEqualTo("Python编程：从入门到实践");
        assertThat(results).extracting(r -> r.get("categoryName")).contains("科技计算机");
        java.util.Collection<?> themes = (java.util.Collection<?>) resp.get("themes");
        assertThat(themes.toString()).contains("编程");
    }

    @Test
    @DisplayName("科幻类提问应命中科幻图书并给出理由")
    @SuppressWarnings("unchecked")
    void shouldFindSciFiBooks() {
        Map<String, Object> resp = service.search("想看点烧脑的科幻小说", 3);

        List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).get("name")).isEqualTo("三体");
        assertThat((String) results.get(0).get("reason")).isNotBlank();
    }

    @Test
    @DisplayName("主题词应映射到对应分类：历史人物 → 历史传记")
    void shouldMapIntentToCategory() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results =
                (List<Map<String, Object>>) service.search("有没有讲历史人物的书", 3).get("results");
        assertThat(results.get(0).get("name")).isEqualTo("毛泽东传");
    }

    @Test
    @DisplayName("完全无命中时应退化为高分图书而不是返回空")
    @SuppressWarnings("unchecked")
    void shouldFallbackWhenNothingMatches() {
        Map<String, Object> resp = service.search("zzzzzz", 3);
        List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");

        assertThat(results).hasSize(3);
        assertThat((String) results.get(0).get("reason")).contains("暂无直接匹配");
    }

    @Test
    @DisplayName("拆词应过滤「的/了/想找」等虚词噪声，不产生无意义二字片段")
    @SuppressWarnings("unchecked")
    void shouldFilterNoiseTokens() {
        List<String> keywords = (List<String>) service.search("我想要的的经典爱情故事", 3).get("keywords");
        assertThat(keywords).doesNotContain("的的", "要的");
        assertThat(keywords).anyMatch(k -> k.contains("经典") || k.contains("爱情"));
    }

    @Test
    @DisplayName("返回结果数量不超过 limit")
    @SuppressWarnings("unchecked")
    void shouldRespectLimit() {
        List<Map<String, Object>> results =
                (List<Map<String, Object>>) service.search("散文", 2).get("results");
        assertThat(results).hasSizeLessThanOrEqualTo(2);
    }
}