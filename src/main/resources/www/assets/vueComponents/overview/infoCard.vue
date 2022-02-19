<template>
  <div class="card m-2 text-white bg-dark">
    <div class="card-body" v-if="loading == false">
      <h6 class="card-title text-center">{{ title }}</h6>
      <h4 class="card-text text-center">{{ value }}</h4>
    </div>

    <div class="card-body text-center" v-if="loading == true">
      <h6 class="card-title text-center">{{ title }}</h6>
      <div class="spinner-border text-danger" role="status"></div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: undefined,
    value: undefined,
  },
  data() {
    return {
      loading: false,
    };
  },
  created() {
    this.valueChecker();
  },
  methods: {
    valueChecker() {
      if (this.value == null || this.value == "") {
        this.loading = true;
      } else {
        this.loading = false;
      }
      setTimeout(this.valueChecker, 1000);
    },
  },
};
</script>
