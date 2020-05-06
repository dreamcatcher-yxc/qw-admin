<template>
  <a-card :bordered="false">
    <a-steps class="steps" :current="current">
      <a-step title="填写转账信息"></a-step>
      <a-step title="确认转账信息"></a-step>
      <a-step title="完成"></a-step>
    </a-steps>
    <div class="content">
      <step1 v-if="current === 0" @next-step="nextStep"></step1>
      <step2 v-if="current === 1" @next-step="nextStep" @prev-step="prevStep"></step2>
      <step3 v-if="current === 2" @prev-step="prevStep" @finish="finish"></step3>
    </div>
  </a-card>
</template>

<script lang="es6">
module.exports = {
  name: 'StepForm',
  components: {
    Step1: load('./Step1'), 
    Step2: load('./Step2'),
    Step3: load('./Step3')
  },
  data () {
    return {
      desc: '将一个冗长或用户不熟悉的表单任务分成多个步骤，指导用户完成。',
      current: 1
    }
  },
  methods: {
    nextStep () {
      if (this.current < 2) {
        this.current += 1
      }
    },
    prevStep () {
      if (this.current > 0) {
        this.current -= 1
      }
    },
    finish () {
      this.current = 0
    }
  }
}
</script>

<style scoped>
  .steps{
    max-width: 750px;
    margin: 16px auto;
  }
</style>
