import { ref } from 'vue'

const cartCount = ref(0)

export function useCartCount() {
  const setCartCount = (count) => {
    cartCount.value = count
  }
  return { cartCount, setCartCount }
}
