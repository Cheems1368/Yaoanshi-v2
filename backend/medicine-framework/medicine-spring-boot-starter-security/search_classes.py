import pathlib
needle = "Password encoder strength = ".encode()
found = False
for path in pathlib.Path('target/classes').rglob('*'):
    if path.is_file():
        data = path.read_bytes()
        if needle in data:
            print('found in', path)
            found = True
            break
if not found:
    print('not found')
