import os

p = r'E:\软件工程综合实践\demo\vue\src\views\Cart.vue'
with open(p, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace(
    "import { ref, computed, onMounted } from 'vue'",
    "import { ref, computed, onMounted, watch } from 'vue'"
)

with open(p, 'w', encoding='utf-8') as f:
    f.write(c)

print('fixed')