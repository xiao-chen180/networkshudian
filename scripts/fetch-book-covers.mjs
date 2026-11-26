/**
 * 为书店系统中的每本书抓取豆瓣读书封面。
 *
 * 用法：
 *   node scripts/fetch-book-covers.mjs [--limit N] [--force]
 *
 * 逻辑：
 *   1. 从本地后端 /api/books 读取图书列表（需后端已启动）；
 *   2. 用书名查询豆瓣读书联想接口；
 *   3. 用书名相似度 + 作者名匹配挑选最合适的条目；
 *   4. 下载大尺寸封面到 vue/public/covers/{isbn}.jpg；
 *   5. 输出 scripts/cover-report.json，记录匹配与失败明细。
 *
 * 已存在的封面默认跳过，--force 可强制重新抓取。
 */

import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')
const coverDir = path.join(root, 'vue', 'public', 'covers')
const reportPath = path.join(__dirname, 'cover-report.json')

const args = process.argv.slice(2)
const limitArg = args.indexOf('--limit')
const LIMIT = limitArg >= 0 ? Number(args[limitArg + 1]) : Infinity
const FORCE = args.includes('--force')

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

/* ---------------- 文本归一化与匹配 ---------------- */

/** 去掉括号注释（用于书名匹配，如「红楼梦（全二册）」） */
const normText = (s) =>
  (s || '')
    .replace(/[（(【\[][^）)】\]]*[）)】\]]/g, '')
    .replace(/[\s·・,，.。:：;；!！?？'"“”‘’\-—_/\\+&]/g, '')
    .replace(/[锺]/g, '钟')
    .toLowerCase()

/** 作者名只去掉开头的国籍标注，保留括号内的原文名（如「(Bryan Basham)」） */
const stripNationality = (s) => (s || '').replace(/^\s*[\[（(【][^\]）)】]{1,16}[\]）)】]\s*/, '')
/** 去掉「J.」「T.」这类中间名缩写，避免中英混排导致对不上 */
const stripInitials = (s) =>
  (s || '').replace(/(^|[\s·・•●\-])([A-Za-z])\.?(?=[\s·・•●\-]|$|[一-鿿])/g, '$1')

const normPlain = (s) =>
  (s || '')
    .replace(/[\s·・•●‧,，.。:：;；!！?？'"“”‘’\-—_/\\+&（）()【】\[\]]/g, '')
    .replace(/[锺]/g, '钟')
    .toLowerCase()

const normAuthor = (s) => normPlain(stripInitials(stripNationality(s))).replace(/著|译|编|等|撰/g, '')

const overlap = (a, b) => {
  if (!a || !b) return 0
  const pool = new Set([...a])
  return [...new Set([...b])].filter((ch) => pool.has(ch)).length
}

function simplifyTitle(title) {
  const simple = title.split(/[：:（(【\[]/)[0].trim()
  return simple && simple !== title ? simple : null
}

function pickCandidate(book, candidates) {
  const bt = normText(book.name)
  const ba = normAuthor(book.author)
  const scored = []

  for (const c of candidates) {
    if (c.type !== 'b' || !c.pic) continue
    const ct = normText(c.title)
    const asciiEnd = /[a-z0-9]$/.test(bt)
    let ts = 0
    if (ct === bt) ts = 100
    else if (ct.startsWith(bt)) {
      // 防止「C Primer」误配到「C Primer Plus」：英文书名后直接接字母说明是另一个词
      const next = ct[bt.length] || ''
      ts = asciiEnd && /[a-z]/.test(next) ? 55 : 78
    } else if (bt.startsWith(ct)) ts = 78
    else if (ct.includes(bt) || bt.includes(ct)) ts = 55
    if (!ts) continue

    const ca = normAuthor(c.author_name)
    let as = 0
    if (ca && ba && (ca.includes(ba) || ba.includes(ca))) as = 40
    else if (overlap(ca, ba) >= 2) as = 12

    scored.push({ ...c, ts, as, score: ts + as })
  }

  scored.sort((x, y) => y.score - x.score || y.ts - x.ts)

  const authorMatched = scored.filter((c) => c.as >= 40)
  if (authorMatched.length) return authorMatched[0]
  // 古籍：作者名与书名相同（如《庄子》庄子），接受任意版本
  if (ba && (ba === bt || bt.includes(ba)) && scored[0]?.ts === 100) return scored[0]
  // 没有作者匹配时，只有标题唯一且高度一致才接受
  if (scored.length === 1 && scored[0].ts >= 75) return scored[0]
  return null
}

/* ---------------- 网络请求 ---------------- */

async function getJson(url) {
  const res = await fetch(url, {
    headers: {
      'User-Agent': UA,
      Referer: 'https://book.douban.com/',
      Accept: 'application/json, text/plain, */*'
    }
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

async function suggest(title) {
  const url = `https://book.douban.com/j/subject_suggest?${new URLSearchParams({ q: title })}`
  return getJson(url)
}

async function fetchImage(url) {
  const res = await fetch(url, {
    headers: {
      'User-Agent': UA,
      Referer: 'https://book.douban.com/',
      Accept: 'image/avif,image/webp,image/apng,image/*,*/*;q=0.8'
    }
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  const buf = Buffer.from(await res.arrayBuffer())
  if (!(buf[0] === 0xff && buf[1] === 0xd8 && buf[2] === 0xff)) {
    throw new Error('返回内容不是 JPEG')
  }
  return buf
}

const picAt = (pic, size) => pic.replace(/\/subject\/[sml]\//, `/subject/${size}/`)

/** 优先大图；大图过小时再试中图/原图，取最大的一张 */
async function downloadBest(pic, dest) {
  const urls = [picAt(pic, 'l'), picAt(pic, 'm'), pic]
  let best = null
  for (const url of urls) {
    try {
      const buf = await fetchImage(url)
      if (!best || buf.length > best.length) best = buf
      if (best.length >= 8000) break
    } catch {
      /* 尝试下一个尺寸 */
    }
  }
  if (!best) throw new Error('所有尺寸均下载失败')
  if (best.length < 1500) throw new Error(`图片过小 (${best.length} bytes)`)
  await fs.promises.writeFile(dest, best)
  return best.length
}

async function findCover(book) {
  const queries = [book.name]
  const simple = simplifyTitle(book.name)
  if (simple) queries.push(simple)

  for (const q of queries) {
    let candidates
    try {
      candidates = await suggest(q)
    } catch (e) {
      return { status: 'error', reason: `搜索失败: ${e.message}` }
    }
    if (!Array.isArray(candidates)) continue
    const hit = pickCandidate(book, candidates)
    if (hit) return { status: 'matched', hit }
    await sleep(700)
  }
  return { status: 'unmatched', reason: '没有找到书名/作者都匹配的条目' }
}

/* ---------------- 主流程 ---------------- */

async function fetchBooks() {
  const all = []
  for (let page = 0; ; page++) {
    const res = await fetch(`http://localhost:8080/api/books?page=${page}&size=100`)
    if (!res.ok) throw new Error(`读取图书接口失败: HTTP ${res.status}`)
    const data = await res.json()
    all.push(...data.content)
    if (data.content.length === 0 || all.length >= data.totalElements) break
  }
  return all
}

function formatMB(bytes) {
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

async function main() {
  fs.mkdirSync(coverDir, { recursive: true })

  console.log('读取图书列表…')
  const books = await fetchBooks()
  console.log(`共 ${books.length} 本图书，开始抓取封面\n`)

  const todo = books.slice(0, LIMIT)
  const report = { generatedAt: new Date().toISOString(), total: todo.length, matched: [], unmatched: [], failed: [] }
  let downloaded = 0

  for (let i = 0; i < todo.length; i++) {
    const book = todo[i]
    const label = `[${String(i + 1).padStart(3)}/${todo.length}] 《${book.name}》(${book.author})`
    const dest = path.join(coverDir, `${book.isbn}.jpg`)

    if (!FORCE && fs.existsSync(dest) && fs.statSync(dest).size > 3000) {
      report.matched.push({ isbn: book.isbn, title: book.name, author: book.author, file: `${book.isbn}.jpg`, subject: null, note: '已存在，跳过' })
      console.log(`${label} → 已存在，跳过`)
      continue
    }

    try {
      const found = await findCover(book)
      if (found.status === 'matched') {
        const hit = found.hit
        const bytes = await downloadBest(hit.pic, dest)
        downloaded += bytes
        report.matched.push({
          isbn: book.isbn,
          title: book.name,
          author: book.author,
          file: `${book.isbn}.jpg`,
          doubanTitle: hit.title,
          doubanAuthor: hit.author_name,
          subject: hit.url
        })
        console.log(`${label} → ${hit.title} / ${hit.author_name}  ${(bytes / 1024).toFixed(0)}KB`)
      } else {
        report.unmatched.push({ isbn: book.isbn, title: book.name, author: book.author, reason: found.reason })
        console.log(`${label} → 未匹配`)
      }
    } catch (e) {
      report.failed.push({ isbn: book.isbn, title: book.name, author: book.author, reason: e.message })
      console.log(`${label} → 失败: ${e.message}`)
    }

    await sleep(900)
  }

  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf8')
  console.log('\n===== 完成 =====')
  console.log(`成功: ${report.matched.length}  未匹配: ${report.unmatched.length}  失败: ${report.failed.length}`)
  console.log(`本次下载: ${formatMB(downloaded)}，输出目录: ${coverDir}`)
  console.log(`报告: ${reportPath}`)
  if (report.unmatched.length) {
    console.log('\n未匹配：')
    for (const u of report.unmatched) console.log(`  - ${u.isbn} 《${u.title}》 ${u.author}`)
  }
  if (report.failed.length) {
    console.log('\n失败：')
    for (const u of report.failed) console.log(`  - ${u.isbn} 《${u.title}》 ${u.reason}`)
  }
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})