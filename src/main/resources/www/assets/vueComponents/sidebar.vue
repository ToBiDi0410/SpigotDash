<template>
  <div
    class="d-flex flex-column flex-shrink-0 p-3 text-white bg-dark"
    style="width: 280px"
  >
    <div
      href="/"
      class="d-flex align-items-center justify-content-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none w-100"
    >
      <div class="bi me-2 logo fs-4" width="40" height="32"></div>
    </div>
    <hr />
    <ul class="nav nav-pills flex-column mb-auto">
      <li class="nav-item">
        <a href="#" class="nav-link active" aria-current="page">
          <ion-icon name="albums-outline"></ion-icon>
          Overview
        </a>
      </li>

      <li class="border-top my-3"></li>
      <li class="text-light mb-1">Performance</li>
      <li>
        <a href="#" class="nav-link text-white">
          <ion-icon name="analytics-outline"></ion-icon>
          Grahps
        </a>
      </li>
      <li>
        <a href="#" class="nav-link text-white">
          <ion-icon name="globe-outline"></ion-icon>
          Worlds
        </a>
      </li>
      <li>
        <a href="#" class="nav-link text-white">
          <ion-icon name="calculator-outline"></ion-icon>
          Advanced Calc
          <a class="text-primary">NEW</a>
        </a>
      </li>

      <li class="border-top my-3"></li>
      <li class="text-light mb-1">Administration</li>
    </ul>
    <hr />
    <div class="dropdown">
      <a
        href="#"
        class="d-flex align-items-center text-white text-decoration-none dropdown-toggle"
        id="dropdownUser1"
        data-bs-toggle="dropdown"
        aria-expanded="false"
      >
        <img
          src="https://github.com/mdo.png"
          alt=""
          width="32"
          height="32"
          class="rounded-circle me-2"
        />
        <strong>Tobias</strong>
      </a>
      <ul
        class="dropdown-menu dropdown-menu-dark text-small shadow"
        aria-labelledby="dropdownUser1"
      >
        <li><a class="dropdown-item" href="#">New project...</a></li>
        <li><a class="dropdown-item" href="#">Settings</a></li>
        <li><a class="dropdown-item" href="#">Profile</a></li>
        <li><hr class="dropdown-divider" /></li>
        <li><a class="dropdown-item" href="#">Sign out</a></li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedPage: "overview/overview",
    };
  },
};
</script>

<style scoped>
.logo {
  background-image: url("./assets/img/NewLogoWeb.png");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center center;
  width: 90px;
  height: 50px;
}
</style>
