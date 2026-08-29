# 猜数字小游戏
import random

ran = random.randint(1, 199)

cc = int(input("请输入你猜测的数字:"))

while cc != ran:
    if cc > ran:
        cc = int(input("值大了，请再次输入你猜测的数字："))
    else:
        cc = int(input("值小了，请再次输入你猜测的数字："))
print("恭喜你，猜中了!!!")
