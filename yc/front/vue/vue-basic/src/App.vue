<template>
  <h1>{{ message }}</h1>
  <p v-bind:class="class">{{ ok ? 'yes' : 'no' }}</p>
  <div v-html="rawHtml"></div> 
</template>
// v-html 插入识别的html


<script>
export default {
  data() {
    return {
      message: 'Hello Vue!',
      ok: true,
      rawHtml: '<a href="https://vuejs.org">Vue.js</a>',
      class: 'container',
    }
  }
}
</script>

<style>
.container {
  color: red;

}
</style>