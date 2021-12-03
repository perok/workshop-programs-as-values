module.exports = {
<<<<<<< HEAD
  // TODO what is purge setup here, is this correct?
  // purge: ['./dist/*.html'],
=======
  mode: 'jit',
  purge: [
    'index.html',
    './target/scala-3.1.0/frontend-fastopt/main.js'
  ],
>>>>>>> 7d30002 (Use vite)
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
