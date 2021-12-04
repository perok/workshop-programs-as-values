module.exports = {
  mode: 'jit',
  purge: [
    'index.html',
    './src/main/**/*.scala'
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
