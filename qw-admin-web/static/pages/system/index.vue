<template>
  <div>
    <h1 v-text="message"></h1>
    <a-button type="primary" @click="showModal">Primary</a-button>
    <a-modal 
        title="Modal" 
        v-model="visible" 
        @ok="hideModal" 
        :body-style="modalBodyStyle"
        :width="1100"
        okText="确认"
        cancelText="取消">
        <iframe class="iframe" src="/admin/base/privilege/view/"></iframe>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = {
    data () {
        return {
            visible: false,
            message: 'hello world!',
            modalBodyStyle: {
                width: '100%',
                border: 'none'
            }
        }
    },
    methods: {
        showModal () {
            this.visible = true;
        },
        hideModal () {
            this.visible = false;
        }
    }
}
</script>
<style scoped>
    .iframe {
        width: 100%;
        min-height: 600px;
        border: none;
    }
</style>