/**
 * 图片懒加载指令
 * 用法: <img v-lazy="imageUrl" />
 */
export const lazy = {
  mounted(el, binding) {
    const imageLoading = '/images/loading-placeholder.svg'

    // 设置默认占位图
    el.src = imageLoading
    el.style.transition = 'opacity 0.3s ease'

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = new Image()
          img.onload = () => {
            el.src = binding.value
            el.style.opacity = '1'
          }
          img.onerror = () => {
            el.src = 'https://via.placeholder.com/200x280?text=No+Image'
            el.style.opacity = '1'
          }
          img.src = binding.value
          observer.unobserve(el)
        }
      })
    }, {
      rootMargin: '100px 0px',
      threshold: 0.1
    })

    el.style.opacity = '0'
    observer.observe(el)
    el._lazyObserver = observer
  },

  unmounted(el) {
    if (el._lazyObserver) {
      el._lazyObserver.disconnect()
    }
  }
}
