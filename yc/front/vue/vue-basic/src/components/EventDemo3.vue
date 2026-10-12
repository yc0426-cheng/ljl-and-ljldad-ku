<template>
  <h3>事件传参</h3>
  <div>
    <p>点击按钮，触发事件</p>
    <button @click="handleClick">点击我下面的值增加</button>
    <p>当前值：{{ count }}</p>

    <div @click="handleDIV">
      <p @click.stop="handleP">可以加.stop 阻止事件冒泡到父盒子</p>
    </div>
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
    handleClick(e) {
      // 若是使用内联事件处理，传参需要使用 $event 来传递事件对象
      // 比如：<button @click="handleClick(参数,$event)">点击我下面的值增加</button>
      e.target.innerHTML = "Add" + this.count;
      this.count++;
    },

    handleDIV() {
      console.log("DIV");
    },

    handleP() {
      console.log("P");
    },
  },
};
</script>
