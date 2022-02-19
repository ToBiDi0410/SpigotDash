<template>
  <main>
    <div class="d-flex flex-row bd-highlight mb-3">
      <div>
        <sidebar class="sidebar"></sidebar>
      </div>
      <div class="w-100 bg-light">
        <mainContent currentPage="overview/overview"></mainContent>
      </div>
    </div>
  </main>
</template>

<script>
import sidebar from "sidebar.vue";
import mainContent from "mainContent.vue";

export default {
  components: { sidebar, mainContent },
  data: function () {
    return {
      greeting: "Hello",
    };
  },
};
</script>

<style scoped>
.sidebar {
  height: 100vh;
}
</style>
