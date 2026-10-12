<template>
    <p>Children</p>
    <p>{{ text }}</p>
    <ul>
        <li v-for="(value, index) of names" :key="index">{{ value }}</li>
    </ul>
    <button @click="setProps">修改text</button>
</template>

<script>

export default {
    props: {
        text: {
            type: [String],
            default: ''
        },
        names: {
            type: [Array],
            required: true, //必选项，必须传递
            default() {
                return ['']
            } // 数组和对象返回默认值需要使用函数
        }
    }, //只能从父级传入子级,且props只读，无法修改
    methods: {
        setProps() {
            console.log(this.text)
            this.text = '修改props后的新数据'
            console.log(this.text)
        }
    }
}
</script>