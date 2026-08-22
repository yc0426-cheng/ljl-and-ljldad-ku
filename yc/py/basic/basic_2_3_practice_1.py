# 打印99乘法表

for heng in range(1, 10):
    for shu in range(1, heng+1):
        print(f'{shu} × {heng} = {heng * shu}', end='  ')
    print()
