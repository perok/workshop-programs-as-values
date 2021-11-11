const { merge } = require('webpack-merge');
const path = require('path');

const generatedConfig = require('./scalajs.webpack.config');

module.exports = merge(generatedConfig, {
  devServer: {
    proxy: {
      '/api': {
        target: 'localhost:8081',
        secure: false
      }
    }
  },
  output: {
    // TODO how to get this to work? Shuld get all assets within
    // resources/assets - https://github.com/scalacenter/scalajs-bundler/issues/154
    path:  path.join(generatedConfig.output.path, "assets")
  },
  module: {
    rules: [
      // {
      //   test: /\.js$/,
      //   // use loader defaults
      //   use: ["scalajs-friendly-source-map-loader"]
      // },
      {
        test: /\.scss$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: 'bundle.css',
            },
          },
          { loader: 'extract-loader' },
          { loader: 'css-loader' },
          {
            loader: 'sass-loader',
            options: {
              sassOptions: {
                includePaths: ['./node_modules']
              }
            }
          },
        ]
      }
    ]
  },
  resolve: {
    alias: {
      Sources: path.resolve(__dirname, '../../../../src/main')
    }
  }
});
