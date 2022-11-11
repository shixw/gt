#!/usr/bin/env sh

# 确保脚本抛出遇到的错误
set -e

# 生成静态文件
npm run build

# 进入生成的文件夹
cd dist

if [ -z "$GITHUB_TOKEN" ]; then
  msg='deploy'
  githubUrl=git@github.com:shixw/gt.git
else
  msg='来自github actions的自动部署'
  githubUrl=https://shixw:${GITHUB_TOKEN}@github.com/shixw/gt.git
fi
echo githubUrl
git config --global user.name "shixw"
git config --global user.email "shixw_usr@126.com"
git init
git add -A
git commit -m "${msg}"
git push -f $githubUrl master:gh-pages # 推送到github gh-pages分支

cd -
rm -rf dist
