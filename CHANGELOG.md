# Change log

## 0.4.0 ([#6](https://git.mobcastdev.com/Marvin/watcher-service/pull/6) 2015-01-16 14:21:23)

Run as quill user

### New feature

- Run as the quill user (for FTP permissions' sake)
- Use default position for folders.

## 0.3.0 ([#5](https://git.mobcastdev.com/Marvin/watcher-service/pull/5) 2015-01-06 12:01:27)

Dot files ignored

### New Feature

- Ignore dotfiles if they are found in publisher folders (MV-218)

## 0.2.1 ([#4](https://git.mobcastdev.com/Marvin/watcher-service/pull/4) 2014-12-11 16:30:48)

Standardise name of service

### Improvement

- Change the name of the service so it matches platform standards. (`watcher` -> `watcher-service`)

## 0.2.0 ([#3](https://git.mobcastdev.com/Marvin/watcher2/pull/3) 2014-12-11 16:17:35)

MVP: Single threaded watching

### New Features

- Checks the specified folder regularly for files
- Any found files are (safely) moved to the storage folder announced via RabbitMQ
- Each announcement includes content type details of the found file
- Doesn't use actors internally!

![azfmftn](https://git.mobcastdev.com/github-enterprise-assets/0000/0007/0000/0360/80dbfbf6-813a-11e4-86ad-f32b942384e2.gif)


## 0.1.0 ([#1](https://git.mobcastdev.com/Marvin/watcher2/pull/1) 2014-11-26 11:55:29)

Bootstrap

### New features

- Builds and runs!
- Then immediately exits!

AAaaaaaaaaaaalrighty then!

![bu55hsv](https://git.mobcastdev.com/github-enterprise-assets/0000/0007/0000/0318/7b584d3e-755e-11e4-90e6-8a220f3bf7d4.gif)


