import argparse
import re
from pathlib import Path

import pandas as pd

# A list of all standard modules is obtained from the Python Module Index: https://docs.python.org/3.10/py-modindex.html
STDLIB_MODULES = [
    '__future__',
    '__main__',
    '_thread',
    'abc',
    'aifc',
    'argparse',
    'array',
    'ast',
    'asynchat',
    'asyncio',
    'asyncore',
    'atexit',
    'audioop',
    'base64',
    'bdb',
    'binascii',
    'binhex',
    'bisect',
    'builtins',
    'bz2',
    'calendar',
    'cgi',
    'cgitb',
    'chunk',
    'cmath',
    'cmd',
    'code',
    'codecs',
    'codeop',
    'collections',
    'colorsys',
    'compileall',
    'concurrent.futures',
    'configparser',
    'contextlib',
    'contextvars',
    'copy',
    'copyreg',
    'crypt',
    'csv',
    'ctypes',
    'curses',
    'dataclasses',
    'datetime',
    'dbm',
    'decimal',
    'difflib',
    'dis',
    'distutils',
    'doctest',
    'email',
    'encodings.idna',
    'encodings.mbcs',
    'encodings.utf_8_sig',
    'ensurepip',
    'enum',
    'errno',
    'faulthandler',
    'fcntl',
    'filecmp',
    'fileinput',
    'fnmatch',
    'fractions',
    'ftplib',
    'functools',
    'gc',
    'getopt',
    'getpass',
    'gettext',
    'glob',
    'graphlib',
    'grp',
    'gzip',
    'hashlib',
    'heapq',
    'hmac',
    'html',
    'http',
    'imaplib',
    'imghdr',
    'imp',
    'importlib',
    'inspect',
    'io',
    'ipaddress',
    'itertools',
    'json',
    'keyword',
    'lib2to3',
    'linecache',
    'locale',
    'logging',
    'lzma',
    'mailbox',
    'mailcap',
    'marshal',
    'math',
    'mimetypes',
    'mmap',
    'modulefinder',
    'msilib',
    'msvcrt',
    'multiprocessing',
    'netrc',
    'nis',
    'nntplib',
    'numbers',
    'operator',
    'optparse',
    'os',
    'ossaudiodev',
    'pathlib',
    'pdb',
    'pickle',
    'pickletools',
    'pipes',
    'pkgutil',
    'platform',
    'plistlib',
    'poplib',
    'posix',
    'pprint',
    'profile',
    'pstats',
    'pty',
    'pwd',
    'py_compile',
    'pyclbr',
    'pydoc',
    'queue',
    'quopri',
    'random',
    're',
    'readline',
    'reprlib',
    'resource',
    'rlcompleter',
    'runpy',
    'sched',
    'secrets',
    'select',
    'selectors',
    'shelve',
    'shlex',
    'shutil',
    'signal',
    'site',
    'smtpd',
    'smtplib',
    'sndhdr',
    'socket',
    'socketserver',
    'spwd',
    'sqlite3',
    'ssl',
    'stat',
    'statistics',
    'string',
    'stringprep',
    'struct',
    'subprocess',
    'sunau',
    'symtable',
    'sys',
    'sysconfig',
    'syslog',
    'tabnanny',
    'tarfile',
    'telnetlib',
    'tempfile',
    'termios',
    'test',
    'textwrap',
    'threading',
    'time',
    'timeit',
    'tkinter',
    'token',
    'tokenize',
    'trace',
    'traceback',
    'tracemalloc',
    'tty',
    'turtle',
    'turtledemo',
    'types',
    'typing',
    'unicodedata',
    'unittest',
    'urllib',
    'uu',
    'uuid',
    'venv',
    'warnings',
    'wave',
    'weakref',
    'webbrowser',
    'winreg',
    'winsound',
    'wsgiref',
    'xdrlib',
    'xml',
    'xmlrpc.client',
    'xmlrpc.server',
    'zipapp',
    'zipfile',
    'zipimport',
    'zlib',
    'zoneinfo',
]


def _is_stdlib_name(fq_name: str) -> bool:
    """
    Checks if the FQ name starts with the name of the standard module.
    """
    # Add a dot at the end of the FQ name and at the end of the module names.
    # This is necessary to correctly identify FQ names that start with the name of the standard library.
    stdlib_modules_with_dot = list(map(lambda module_name: f'{module_name}.', STDLIB_MODULES))
    fq_name_with_dot = f'{fq_name}.'

    return any(fq_name_with_dot.startswith(stdlib_module) for stdlib_module in stdlib_modules_with_dot)


def __is_dunder_name(name: str) -> bool:
    """
    Checks if the name starts and ends with double underscores.
    """
    return re.match(r'__.*__', name) is not None


def _is_private_name(fq_name: str) -> bool:
    """
    Checks if the FQ name is private.

    A FQ name is considered private if at least one part of it starts with underscore.
    Private modules of the standard library and dunder names are ignored.
    """
    fq_parts = fq_name.split('.')

    # `_thread` is a module of the Python Standard Library
    if fq_parts[0] == '_thread':
        return False

    return any(
        [fq_part.startswith('_') and not __is_dunder_name(fq_part) for fq_part in fq_parts],
    )


def main(
    path_to_fq_names: Path,
    path_to_result: Path,
    column_name: str,
    filter_private_names: bool,
    filter_stdlib_names: bool,
    filter_dunder_names: bool,
) -> None:
    fq_names = pd.read_csv(path_to_fq_names)

    print(f'Received {len(fq_names)} FQ names.')

    if filter_private_names:
        mask = fq_names.apply(lambda row: _is_private_name(row[column_name]), axis=1)
        fq_names = fq_names[~mask]
        print(f'Filtered {mask.values.sum()} private names.')

    if filter_stdlib_names:
        mask = fq_names.apply(lambda row: _is_stdlib_name(row[column_name]), axis=1)
        fq_names = fq_names[~mask]
        print(f'Filtered {mask.values.sum()} stdlib names.')

    if filter_dunder_names:
        mask = fq_names.apply(
            lambda row: any(__is_dunder_name(fq_part) for fq_part in row[column_name].split('.')),
            axis=1,
        )
        fq_names = fq_names[~mask]
        print(f'Filtered {mask.values.sum()} dunder names.')

    path_to_result.parent.mkdir(parents=True, exist_ok=True)
    fq_names.to_csv(path_to_result, index=False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--input',
        type=lambda value: Path(value).absolute(),
        help='path to csv file with FQ names',
        required=True,
    )
    parser.add_argument(
        '--output',
        type=lambda value: Path(value).absolute(),
        help='path to output csv file with filtered FQ names',
        required=True,
    )
    parser.add_argument(
        '--column-name',
        type=str,
        help='the name of the column to filter by',
        default='fq_name',
    )
    parser.add_argument(
        '--filter-private-names',
        help='if specified, private names will be filtered out',
        action='store_true',
    )
    parser.add_argument(
        '--filter-dunder-names',
        help=(
            'if specified, dunder names (names which begin and end with double underscores) will be filtered out'
        ),
        action='store_true',
    )
    parser.add_argument(
        '--filter-stdlib-names',
        help='if specified, Python Standard Library names will be filtered out',
        action='store_true',
    )

    args = parser.parse_args()

    main(
        args.input,
        args.output,
        args.column_name,
        args.filter_private_names,
        args.filter_stdlib_names,
        args.filter_dunder_names,
    )
