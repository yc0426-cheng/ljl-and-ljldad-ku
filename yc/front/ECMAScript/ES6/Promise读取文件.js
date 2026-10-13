const fs = require("fs");

// 读取文件的函数，返回一个 Promise
// fs.readFile('../../../java/question/javaBaguBasic1.md', (error, data) => {
//     if (error) throw error;
//     console.log(data.toString());
// })

const p = new Promise((resolve, reject) => {
  fs.readFile("../../../java/question/javaBaguBasic1.md", (error, data) => {
    if (error) throw error;
    resolve(data);
  });
});

p.then(
  (value) => {
    console.log(value.toString());
  },
  (reason) => {
    console.log("读取失败", reason);
  },
);
