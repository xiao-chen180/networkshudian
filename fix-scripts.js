const fs = require('fs');

// 修复 BookDetail.vue - 只需确保 goToBook 函数正确
const fixBookDetail = () => {
  const filePath = 'E:\\软件工程综合实践\\demo\\vue\\src\\views\\BookDetail.vue';
  let content = fs.readFileSync(filePath, 'utf8');
  
  // 确保 goToBook 函数存在且正确
  if (!content.includes('const goToBook =')) {
    // 在 script 部分添加 goToBook 函数
    content = content.replace(
      /(const formatPrice =.*?)\n\nonMounted/,
      '$1\n\nconst goToBook = (id) => router.push(`/books/${id}`)\n\nonMounted'
    );
  }
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log('BookDetail.vue 已修复');
};

// 修复 Books.vue - 确保 goDetail 函数正确
const fixBooks = () => {
  const filePath = 'E:\\软件工程综合实践\\demo\\vue\\src\\views\\Books.vue';
  let content = fs.readFileSync(filePath, 'utf8');
  
  // 确保 goDetail 函数存在且正确
  if (!content.includes('const goDetail =')) {
    content = content.replace(
      /(const formatPrice =.*?)\n\nonMounted/,
      '$1\n\nconst goDetail = (id) => router.push(`/books/${id}`)\n\nonMounted'
    );
  }
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log('Books.vue 已修复');
};

// 修复 Home.vue - 确保 goBookDetail 函数正确
const fixHome = () => {
  const filePath = 'E:\\软件工程综合实践\\demo\\vue\\src\\views\\Home.vue';
  let content = fs.readFileSync(filePath, 'utf8');
  
  // 确保 goBookDetail 函数存在且正确
  if (!content.includes('const goBookDetail =')) {
    content = content.replace(
      /(const formatPrice =.*?)\n\nonMounted/,
      '$1\n\nconst goBookDetail = (id) => router.push(`/books/${id}`)\n\nonMounted'
    );
  }
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log('Home.vue 已修复');
};

// 运行修复
fixBookDetail();
fixBooks();
fixHome();
console.log('所有文件修复完成');
