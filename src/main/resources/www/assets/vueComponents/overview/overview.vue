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
        <infoCardVue v-bind:value="calculatedValues.UPTIME" title="Uptime"></infoCardVue>
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
      },
      calculatedValues: {
        UPTIME: "",
      },
    };
  },
  created() {
    this.calculationLoop();
  },
  methods: {
    timeSince(date) {
      var seconds = Math.floor((new Date() - date) / 1000);

      var interval = seconds / 31536000;

      if (interval > 1) {
        return Math.floor(interval) + " y";
      }
      interval = seconds / 2592000;
      if (interval > 1) {
        return Math.floor(interval) + " m";
      }
      interval = seconds / 86400;
      if (interval > 1) {
        return Math.floor(interval) + " d";
      }
      interval = seconds / 3600;
      if (interval > 1) {
        return Math.floor(interval) + " h";
      }
      interval = seconds / 60;
      if (interval > 1) {
        return Math.floor(interval) + " min";
      }
      return Math.floor(seconds) + " s";
    },
    calculationLoop() {
      this.calculatedValues.UPTIME = this.timeSince(this.currentValues.START_DATE);
      setTimeout(this.calculationLoop, 1000);
    },
  },
};
</script>

<style scoped></style>
