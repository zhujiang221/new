import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './assets/base.css';
import './assets/responsive.css';
import './assets/modal-responsive.css';
import './assets/modern-ui.css';

const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.mount('#app');
