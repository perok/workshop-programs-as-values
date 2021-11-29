# Programs as values

## Getting started

Ensure that the following steps have been done:

- Install Scala tools (`./cs setup` is enough): https://docs.scala-lang.org/scala3/getting-started.html
- Install Node tools https://nodejs.org/en/download/

## Editor setup

Use either Intellij or VSCode:

- For Intellij:
  - Ensure that `Scala` plugin is installed
  - Ensure that your are running nightly plugin https://confluence.jetbrains.com/display/SCA/Scala+Plugin+Nightly
  - Recommended: Format on save https://scalameta.org/scalafmt/docs/installation.html#format-on-save
- For VSCode
  - Install metals plugin https://scalameta.org/metals/docs/editors/vscode#installation
  - Recommended: format on save https://stackoverflow.com/a/54665086

## Developing
- `git clone git@github.com:perok/workshop-programs-as-values.git`
- Open in editor of choice :)

- For Intellij:
  - View `SBT shell` tab and run `~fastLinkJS`. This will recompile to JS on file change
- For VSCode:
  - Open terminal tab and run `sbt`, then `~fastLinkJS`. This will recompile to JS on file change
 
- Open terminal tab and run the following. This will run Webpack Dev Server.
  - `cd modules/frontend`
  - `npm install`
  - `npm run start`
- Go to the file `modules/frontend/src/main/scala/frontend/Task.scala`
- Avoid looking in `modules/frontend/src/main/scala/frontend/infrastructure` folder ;)
- Enjoy

## Problems

- Getting errors like the following in `sbt shell` window? Then use the terminal and run `sbt` directly.
```
[error] stack trace is suppressed; run last frontend / frontendInstall for the full output
[error] (frontend / frontendInstall) java.io.IOException: Cannot run program "npm" (in directory "/home/perok/dev/bekk/faggruppe-fp2021/workshop-scala/modules/frontend"): error=2, No such file or directory
[error] Total time: 0 s, completed 20. nov. 2021, 11:58:44
```

