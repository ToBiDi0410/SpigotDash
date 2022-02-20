<template>
  <div class="infoCards container w-100 p-4 pt-6">
    <div class="row">
      <div class="col-sm">
        <infoCardVue v-bind:value="currentValues.TPS" title="TPS"></infoCardVue>
      </div>
      <div class="col-sm">
        <infoCardVue
          v-bind:value="currentValues.PLUGIN_COUNT"
          title="Plugins"
        ></infoCardVue>
      </div>
      <div class="col-sm">
        <infoCardVue
          v-bind:value="currentValues.PLAYER_COUNT"
          title="Players"
        ></infoCardVue>
      </div>
      <div class="col-sm">
        <infoCardVue
          v-bind:value="currentValues.TIME_SINCE_START"
          title="Uptime"
        ></infoCardVue>
      </div>
    </div>
    <div class="row">
      <div class="col-sm">
        <playersGraph></playersGraph>
      </div>
    </div>
  </div>
</template>

<script>
import infoCardVue from "overview/infoCard.vue";
import playersGraph from "overview/playersGraph.vue";

export default {
  components: { infoCardVue, playersGraph },
  props: {},
  data() {
    return {
      graphSeries: [],
      currentValues: {
        TPS: 20,
        PLAYER_COUNT: 5,
        PLUGIN_COUNT: 2,
        START_DATE: new Date(),
        TIME_SINCE_START: null,
      },
      calculatedValues: {
        UPTIME: "",
      },
    };
  },
  created() {
    this.updateLoop();
  },
  methods: {
    async updateLoop() {
      this.currentValues.TPS = await window.store.globalDataAPI.doRequestOnlyData(
        "COLLECTOR_DATA",
        {
          COLLECTOR: "TPS",
          DATAID: "TPS_AVG",
        },
        true,
        5000
      );
      if (isNaN(this.currentValues.TPS)) this.currentValues.TPS = null;

      this.currentValues.TIME_SINCE_START = window.timeSince(
        this.currentValues.START_DATE
      );
      setTimeout(this.updateLoop, 1000);
    },
  },
};
</script>

<style scoped></style>
