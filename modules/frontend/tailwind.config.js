module.exports = {
  mode: 'jit',
  purge: [
    'index.html',
    './target/scala-3.1.0/frontend-fastopt/main.js'
  ],
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {},
  },
  variants: {
    extend: {},
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
