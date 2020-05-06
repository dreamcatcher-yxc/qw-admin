<template>
  <div>
    <a-form style="max-width: 500px; margin: 40px auto 0;">
      <a-alert
        :closable="true"
        message="确认转账后，资金将直接打入对方账户，无法退回。"
        style="margin-bottom: 24px;"
      ></a-alert>
      <a-form-item
        label="付款账户"
        :label-col="{span: 5}"
        :wrapper-col="{span: 19}"
        class="stepFormText"
      >
        ant-design@alipay.com
      </a-form-item>
      <a-form-item
        label="收款账户"
        :label-col="{span: 5}"
        :wrapper-col="{span: 19}"
        class="stepFormText"
      >
        test@example.com
      </a-form-item>
      <a-form-item
        label="收款人姓名"
        :label-col="{span: 5}"
        :wrapper-col="{span: 19}"
        class="stepFormText"
      >
        Alex
      </a-form-item>
      <a-form-item
        label="转账金额"
        :label-col="{span: 5}"
        :wrapper-col="{span: 19}"
        class="stepFormText"
      >
        ￥ 5,000.00
      </a-form-item>
      <a-form-item :wrapper-col="{span: 19, offset: 5}">
        <a-button :loading="loading" type="primary" @click="nextStep">提交</a-button>
        <a-button style="margin-left: 8px" @click="prevStep">上一步</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="es6">
module.exports = {
  name: 'Step2',
  data () {
    return {
      loading: false
    }
  },
  methods: {
    nextStep () {
      let _this = this
      _this.loading = true
      setTimeout(function () {
        _this.$emit('next-step')
      }, 1500)
    },
    prevStep () {
      this.$emit('prev-step')
    }
  }
};
</script>

<style scoped>
  .stepFormText {
    margin-bottom: 24px;
  }
  .stepFormText :global .ant-form-item-label,
  .stepFormText :global .ant-form-item-control {
    line-height: 22px;
  }
</style>
