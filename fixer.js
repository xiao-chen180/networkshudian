const fs = require('fs');

const filePath = 'E:\\软件工程综合实践\\demo\\vue\\src\\views\\Books.vue';
let content = fs.readFileSync(filePath, 'utf8');

// 检查并修复 goDetail 函数
if (!content.includes('router.push('/books/')) {
  content = content.replace(/const goDetail = \(id\) => \{[\s\S]*?\}/, 'const goDetail = (id) => { router.push('/books/' + id) }');
}

// 修复编码问题 - 重新写入确保UTF-8编码
fs.writeFileSync(filePath, content, 'utf8');
console.log('Books.vue fixed');
