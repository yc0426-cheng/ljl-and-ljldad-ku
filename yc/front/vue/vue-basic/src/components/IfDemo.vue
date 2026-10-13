<template>
  <h3>条件渲染</h3>
  <div v-if="ok">你看得见我吗 v-if为false时元素不渲染</div>

  <div v-if="type === 'C'">我是C</div>
  <div v-else-if="type === 'B'">我是B</div>
  <div v-else-if="type === 'A'">我是A</div>
  <div v-else>都不是</div>

  <div v-show="ok">我也是可见的 v-show为false时 display: none</div>
</template>

<script>
export default {
  data() {
    return {
      ok: true,
      type: "B",
    };
  },
};
</script>
