<template>
  <div class="stepper" :class="{ disabled }">
    <button type="button" class="step" :disabled="disabled || modelValue <= min" aria-label="减少数量" @click="change(-1)">−</button>
    <input
      class="value"
      type="number" inputmode="numeric"
      :value="modelValue" :min="min" :max="max"
      :disabled="disabled" aria-label="数量"
      @change="onInput" @blur="normalize"
    />
    <button type="button" class="step" :disabled="disabled || (max != null && modelValue >= max)" aria-label="增加数量" @click="change(1)">+</button>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Number, default: 1 },
  min: { type: Number, default: 1 },
  max: { type: Number, default: 99 },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const clamp = (value) => {
  const n = Number.parseInt(value, 10)
  if (!Number.isFinite(n)) return props.min
  const upper = props.max == null ? n : props.max
  return Math.min(upper, Math.max(props.min, n))
}
const change = (delta) => { if (!props.disabled) emit('update:modelValue', clamp(props.modelValue + delta)) }
const onInput = (event) => { emit('update:modelValue', clamp(event.target.value)) }
const normalize = (event) => { const v = clamp(event.target.value); event.target.value = v; emit('update:modelValue', v) }
</script>

<style scoped>
.stepper {
  display: inline-flex; align-items: stretch; height: 32px;
  border: 1px solid var(--color-line); background: var(--color-surface-2);
}
.stepper.disabled { opacity: .45; }

.step {
  width: 30px; border: 0; background: transparent; color: var(--color-ink-soft);
  font-size: 16px; line-height: 1; cursor: pointer;
}
.step:hover:not(:disabled) { color: var(--color-gold); }
.step:disabled { color: var(--color-ink-faint); cursor: not-allowed; }

.value {
  width: 42px; border: 0; border-inline: 1px solid var(--color-line);
  background: transparent; color: var(--color-ink); text-align: center;
  font-family: var(--font-mono); font-size: 12px;
  -moz-appearance: textfield; appearance: textfield;
}
.value::-webkit-outer-spin-button,
.value::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.value:focus-visible { outline-offset: -2px; }
</style>