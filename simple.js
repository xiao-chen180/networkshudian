const fs = require('fs');
const content = fs.readFileSync('E:\\软件工程综合实践\\demo\\vue\\src\\views\\Books.vue', 'utf8');
fs.writeFileSync('E:\\软件工程综合实践\\demo\\vue\\src\\views\\Books.vue', content, 'utf8');
console.log('done');
