<template>
  <div class="card m-2 text-white bg-dark h-100">
    <div class="card-body" v-if="loading == false">
      <h6 class="card-title text-center">Players</h6>
      <h4 class="card-text text-center"></h4>
    </div>

    <div class="card-body text-center" v-if="loading == true">
      <h6 class="card-title text-center">Players</h6>
      <div class="spinner-border text-danger" role="status"></div>
    </div>
  </div>
</template>

<script>
export default {
  setup() {},
  data() {
    return {
      loading: true,
    };
  },
  created() {},
};
</script>
