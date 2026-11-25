import { onMounted, onUnmounted } from 'vue'

/**
 * 滚动渐入动画 - 元素进入视口时淡入
 */
export function useScrollReveal(options = {}) {
  const { threshold = 0.1, rootMargin = '0px 0px -50px 0px' } = options

  let observer = null

  const init = (selector = '.reveal') => {
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('revealed')
          observer.unobserve(entry.target)
        }
      })
    }, { threshold, rootMargin })

    document.querySelectorAll(selector).forEach(el => {
      observer.observe(el)
    })
  }

  const destroy = () => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  }

  return { init, destroy }
}

/**
 * 视差滚动效果
 */
export function useParallax(selector = '.parallax') {
  const handleScroll = () => {
    const scrolled = window.scrollY
    document.querySelectorAll(selector).forEach(el => {
      const speed = el.dataset.speed || 0.5
      el.style.transform = `translateY(${scrolled * speed}px)`
    })
  }

  onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })
}

/**
 * 数字递增动画
 */
export function animateNumber(element, target, duration = 1000) {
  const start = 0
  const increment = target / (duration / 16)
  let current = start

  const update = () => {
    current += increment
    if (current >= target) {
      element.textContent = target
      return
    }
    element.textContent = Math.floor(current)
    requestAnimationFrame(update)
  }

  requestAnimationFrame(update)
}

/**
 * 打字机效果
 */
export function typeWriter(element, text, speed = 100) {
  let index = 0
  element.textContent = ''

  const type = () => {
    if (index < text.length) {
      element.textContent += text.charAt(index)
      index++
      setTimeout(type, speed)
    }
  }

  type()
}

/**
 * 飞入购物车动画
 */
export function flyToCart(startElement, endSelector = '.cart-icon') {
  const startRect = startElement.getBoundingClientRect()
  const endElement = document.querySelector(endSelector)

  if (!endElement) return

  const endRect = endElement.getBoundingClientRect()

  const flyer = document.createElement('div')
  flyer.style.cssText = `
    position: fixed;
    width: 40px;
    height: 40px;
    background: var(--primary);
    border-radius: 50%;
    z-index: 10000;
    pointer-events: none;
    left: ${startRect.left + startRect.width / 2}px;
    top: ${startRect.top + startRect.height / 2}px;
    transition: all 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
  `

  document.body.appendChild(flyer)

  requestAnimationFrame(() => {
    flyer.style.left = `${endRect.left + endRect.width / 2}px`
    flyer.style.top = `${endRect.top + endRect.height / 2}px`
    flyer.style.transform = 'scale(0.2)'
    flyer.style.opacity = '0'
  })

  setTimeout(() => {
    flyer.remove()
  }, 600)
}

/**
 * 涟漪效果
 */
export function createRipple(event, element) {
  const rect = element.getBoundingClientRect()
  const ripple = document.createElement('span')

  ripple.style.cssText = `
    position: absolute;
    width: 100px;
    height: 100px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    transform: translate(-50%, -50%) scale(0);
    animation: rippleEffect 0.6s ease-out;
    left: ${event.clientX - rect.left}px;
    top: ${event.clientY - rect.top}px;
    pointer-events: none;
  `

  element.style.position = 'relative'
  element.style.overflow = 'hidden'
  element.appendChild(ripple)

  setTimeout(() => {
    ripple.remove()
  }, 600)
}
