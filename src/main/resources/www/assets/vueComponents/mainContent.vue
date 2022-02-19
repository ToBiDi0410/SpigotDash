<template>
  <div>
    <div
      v-if="loading"
      class="d-flex flex-row justify-content-center align-items-center"
      style="height: 100vh; background-color: rgba(119, 118, 118, 0.596)"
    >
      <div class="spinner-border text-danger" role="status"></div>
    </div>

    <component v-if="!loading" v-bind:is="currentComponent"></component>
  </div>
</template>

<script>
export default {
  setup() {},
  data() {
    return {
      loading: true,
      currentComponent: null,
      currentComponentURL: "",
    };
  },
  created() {
    this.loadPage();
  },
  methods: {
    async loadPage() {
      try {
        var newComponentURL = this.currentPage + ".vue";
        if (newComponentURL != this.currentComponentURL) {
          this.loading = true;
          var newComponent = await import(newComponentURL);
          this.currentComponent = newComponent;
          this.currentComponentURL = newComponentURL;
          this.loading = false;
        }
      } catch (err) {
        console.log(err);
      }
      setTimeout(this.loadPage, 100);
    },
  },
  props: {
    currentPage: String,
  },
};
</script>
