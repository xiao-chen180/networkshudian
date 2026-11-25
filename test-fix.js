const fs = require('fs');

// 测试：检查 Books.vue 是否存在
const booksPath = 'E:\\软件工程综合实践\\demo\\vue\\src\\views\\Books.vue';
if (fs.existsSync(booksPath)) {
  console.log('Books.vue 存在');
  const stats = fs.statSync(booksPath);
  console.log('文件大小:', stats.size, 'bytes');
  
  // 读取前200字符检查内容
  const content = fs.readFileSync(booksPath, 'utf8');
  console.log('前200字符:', content.substring(0, 200));
  
  // 检查是否有 goDetail 函数
  if (content.includes('goDetail')) {
    console.log('包含 goDetail 函数');
  } else {
    console.log('缺少 goDetail 函数');
  }
  
  if (content.includes('router.push')) {
    console.log('包含 router.push');
  } else {
    console.log('缺少 router.push');
  }
} else {
  console.log('Books.vue 不存在');
}
