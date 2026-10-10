// Service Worker for ZAINEB 4 PWA
const CACHE_NAME = 'zaineb4-cache-v3';
const ASSETS_TO_CACHE = [
  'index.html',
  'manifest.json',
  'icon-48.png',
  'icon-72.png',
  'icon-96.png',
  'icon-144.png',
  'icon-192.png',
  'icon-512.png',
  'apple-touch-icon.png',
  'favicon.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE).catch((e) => console.log('Cache addAll error:', e));
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    fetch(event.request).catch(() => {
      return caches.match(event.request);
    })
  );
});
