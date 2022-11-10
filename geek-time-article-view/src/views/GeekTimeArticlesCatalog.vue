<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <el-menu
            :default-active="1"
            class="el-menu-demo"
            mode="horizontal"
            @select="handleSelect"
        >
          <el-menu-item v-for="(item,index) in catalog" :index="item.cid">{{item.title}}</el-menu-item>
        </el-menu>
      </el-header>
      <el-container>
        <el-aside width="500px" style="padding: 10px">
          <el-menu
              :default-active="1"
              class="el-menu-demo"
              @select="handleArticleSelect"
          >
            <el-menu-item v-for="(item,index) in articleCatalog" :index="item.id">{{item.article_title}}</el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <div class="editor-content-view" v-html="articleInfo.article_content" v-if="articleInfo"></div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      catalog: [],
      currentCid: undefined,
      articleCatalog: [],
      articleInfo: undefined
    }
  },
  methods:{
    handleArticleSelect(index){
      fetch('geek-time-articles/'+this.currentCid+'/'+index+'/articleInfo/article.json').then(res => {
        return res.json();
      }).then(articleInfo => {
        this.articleInfo = articleInfo.data
      })
    },
    handleSelect(index,indexPath,item){
      this.currentCid = index
      fetch('geek-time-articles/'+index+'/catalog.json').then(res => {
        return res.json();
      }).then(articleCatalog => {
        this.articleCatalog = articleCatalog
        this.articleInfo = undefined
      })
    }
  },
  mounted() {
    fetch('geek-time-articles/catalog.json').then(res => {
      return res.json();
    }).then(catalog => {
      this.catalog = catalog
    })
  }
}
</script>

<style>
.el-header {
  height: 70px;
}
.el-aside {
  height: calc(100vh - 70px);
}
.el-main {
  padding: 0;
  height: calc(100vh - 70px);
}
.editor-content-view
{
  /*border: 1px solid #ccc;*/
  /*border-radius: 5px;*/
  padding: 0 10px;
  margin-top: 0px;
  overflow-x: auto;
  font-family: "PingFang SC", "Lantinghei SC", "Microsoft Yahei", "Hiragino Sans GB", "Microsoft Sans Serif", "WenQuanYi Micro Hei", Helvetica, sans-serif;
  -webkit-font-smoothing: antialiased;
  -webkit-tap-highlight-color: rgba(255,255,255,0);
  font-weight: 400;
  font-size: 17px;
  line-height: 30px;
  color: #353535;
}

.editor-content-view p,
.editor-content-view li {
  white-space: pre-wrap; /* 保留空格 */
  min-height: 30px;
  margin-left: 0px;
  margin-bottom: 30px;
}

.editor-content-view blockquote {
  border-left: 8px solid #d0e5f2;
  padding: 10px 10px;
  margin: 10px 0;
  /*background-color: grey;*/
}

.editor-content-view strong {
  font-weight: bold;
  color: #000;
}

.editor-content-view .reference {
  color: #888888;
}
.editor-content-view .orange {
  color: #fa8919;
}

.editor-content-view h2 {
  font-weight: bold;
  margin-top: 0px;
  margin-bottom: 20px;
  font-size: 20px;
}
.editor-content-view img {
  max-width: 80%;
}

.editor-content-view h1, h2, h3, h4, h5, h6 {
  font-family: PingFang SC,Helvetica Neue,Verdana,Microsoft Yahei,Hiragino Sans GB,Microsoft Sans Serif,WenQuanYi Micro Hei,sans-serif;
  color: #000;
  line-height: 1.35;
}

.editor-content-view code {
  border: 1px solid #ccc;
  border-radius: 5px;
  font-family: monospace;
  background-color: rgb(0 0 0 / 6%);
  padding: 3px;
  border-radius: 3px;
}
.editor-content-view pre>code {
  display: block;
  padding: 10px;
}

.editor-content-view table {
  border-collapse: collapse;
}
.editor-content-view td,
.editor-content-view th {
  border: 1px solid #ccc;
  min-width: 50px;
  height: 20px;
}
.editor-content-view th {
  background-color: grey;
}

.editor-content-view ul,
.editor-content-view ol {
  padding-left: 20px;
}

.editor-content-view input[type="checkbox"] {
  margin-right: 5px;
}
</style>



