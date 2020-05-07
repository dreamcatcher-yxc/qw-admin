<template>
  <div>
    <div class="wrapper">
      <a-alert message="Success Tips" type="success" show-icon></a-alert>
      <a-alert message="Informational Notes" type="info" show-icon></a-alert>
      <a-alert message="Warning" type="warning" show-icon></a-alert>
      <a-alert message="Error" type="error" show-icon></a-alert>
      <a-alert
        message="Success Tips"
        description="Detailed description and advices about successful copywriting."
        type="success"
        show-icon
      >
      </a-alert>
      <a-alert
        message="Informational Notes"
        description="Additional description and informations about copywriting."
        type="info"
        show-icon
      >
      </a-alert>
      <a-alert
        message="Warning"
        description="This is a warning notice about copywriting."
        type="warning"
        show-icon
      >
      </a-alert>
      <a-alert
        message="Error"
        description="This is an error message about copywriting."
        type="error"
        show-icon
      >
      </a-alert>
    <div>
  </div>
</template>

<script lang="es6">
module.exports = {
  name: 'test1-page',
  data () {
    return {
      message: 'this is a test page!'
    }
  },
  methods: {
    
  }
}
</script>
<style scoped>
div.wrapper {
  padding: 5px 15px;
  background-color: #FFF;
}
div.wrapper > div {
  margin-top: 10px;
}
h1 {
  color: red;
}
</style>