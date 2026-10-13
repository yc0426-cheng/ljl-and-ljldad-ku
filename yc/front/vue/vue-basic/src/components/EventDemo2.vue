<template>
  <h3>方法事件处理</h3>
  <div>
    <p>点击按钮，触发事件</p>
    <button @click="handleClick">点击我下面的值增加</button>
    <p>当前值：{{ count }}</p>
  </div>
</template>

<script>
export default {
  data() {
    return {
      count: 0,
    };
  },
  methods: {
    handleClick() {
      this.count++;
      console.log("当前值：", this.count);
    },
  },
};
</script>
