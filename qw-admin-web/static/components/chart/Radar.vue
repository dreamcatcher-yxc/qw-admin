<template>
    <v-chart :force-fit="true" height="400" :data="data" :padding="[20, 20, 95, 20]" :scale="scale">
      <v-tooltip></v-tooltip>
      <v-axis :data-key="axis1Opts.dataKey" :line="axis1Opts.line" :tick-line="axis1Opts.tickLine" :grid="axis1Opts.grid"></v-axis>
      <v-axis :data-key="axis2Opts.dataKey" :line="axis2Opts.line" :tick-line="axis2Opts.tickLine" :grid="axis2Opts.grid"></v-axis>
      <v-legend data-key="user" marker="circle" :offset="30"></v-legend>
      <v-coord type="polar" radius="0.8"></v-coord>
      <v-line position="item*score" color="user" :size="2"></v-line>
      <v-point position="item*score" color="user" :size="4" shape="circle"></v-point>
    </v-chart>
</template>

<script lang="es6">
const cmp = new Promise((resolve, reject) => {
    require([
      'antv-data-set'
    ], function(DataSet) {
      const sourceData = [
        {item: '引用', a: 70, b: 30, c: 40},
        {item: '口碑', a: 60, b: 70, c: 40},
        {item: '产量', a: 50, b: 60, c: 40},
        {item: '贡献', a: 40, b: 50, c: 40},
        {item: '热度', a: 60, b: 70, c: 40},
        {item: '引用', a: 70, b: 50, c: 40}
      ];

      const dv = new DataSet.View().source(sourceData);
      dv.transform({
        type: 'fold',
        fields: ['a', 'b', 'c'],
        key: 'user',
        value: 'score'
      });

      const scale = [{
        dataKey: 'score',
        min: 0,
        max: 80
      }];

      const data = dv.rows;

      const axis1Opts = {
        dataKey: 'item',
        line: null,
        tickLine: null,
        grid: {
          lineStyle: {
            lineDash: null
          },
          hideFirstLine: false
        }
      };

      const axis2Opts = {
        dataKey: 'score',
        line: null,
        tickLine: null,
        grid: {
          type: 'polygon',
          lineStyle: {
            lineDash: null
          }
        }
      };

      resolve({
        name: 'Radar',
        data () {
          return {
            sourceData,
            data,
            axis1Opts,
            axis2Opts,
            scale
          }
        },
        mounted () {
          // console.log('radar runed...')
        }
      });
  });
});

  module.exports = cmp;
</script>

<style scoped>
</style>
