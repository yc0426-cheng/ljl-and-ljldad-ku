# 计算1-100之间所有偶数之和

sum = 0
num = 1

while num <= 100:
    if num % 2 == 0:
        sum += num
    num += 1
else:
    print(f'累加之和为{sum},最后一个偶数是{num-1}')
