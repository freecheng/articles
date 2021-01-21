#!/usr/bin/env python
import os
import filecmp
import time
import hashlib
import csv
import sys

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

"""
Features:
	- Support multiple platforms: 
	- Windows (95/98/NT/2k/XP/03/Vista/7)
	- Unix (Linux, BSD, Solaris, OSX, etc).
	- Works across platforms. E.g. synchronize a Windows client with a Unix server.
	- Bi-directoional.
	- Data safety.
	- Two user interfaces: console UI and Graphical UI.
	- Synchronize between two different directories on a machine, or between two different machines via network.
	- Supports remote shells: ssh, rsh.

Basic Concepts:
	- Root Directories
	- Update
	- Conflict
	- Reconcile

Usage:
	file_sync dir1 dir2 [options]
"""


class FileSyncPreference:
    pass


class FileMetadata:
    type = 0  # regular file, directory, symbolic link
    size = 0  # file size. If it is directory, the number of entries.


# ctime 	# creation time
# mtime	# last modification time
# atime	# last access time


# options
verbose = 1
ignore_time = 0
ignore_content = 0
recursively = 0


def compare_file_content(source_file, target_file):
    pass


# compare two files
# def compare_file(source_file, target_file):
#     # compare by mtime
#     if not ignore_time:
# 	if os.path.getmtime(source_file) < os.path.getmtime(target_file):
# 	    print source_file + " is older than " + target_file
# 	elif os.path.getmtime(source_file) > os.path.getmtime(target_file):
# 	    print source_file + " is newer than " + target_file
#
#     # compare by size
#     if os.path.getsize(source_file) < os.path.getsize(target_file):
# 	print source_file + " is bigger than " + target_file
#     elif os.path.getsize(source_file) > os.path.getsize(target_file):
# 	print source_file + " is smaller than " + target_file
#
#     # compare by content
#     elif os.path.getsize(source_file) != 0 \
# 	    and not compare_file_content(source_file, source_file):
# 	print source_file + " is different from " + target_file
#
#     # Two files are identical
#     elif verbose:
# 	print source_file + " == " + target_file
#
# # compare two directories
# def compare_dir(source_dir, target_dir):
#     # 1. Store entries into a list separately.
#     source_entries = os.listdir(source_dir)
#     target_entries = os.listdir(target_dir)
#
#     # 2. Sort lists by name
#     source_entries.sort()
#     target_entries.sort()
#
#     # 3. Compare one by one
#     for entry in source_entries:
# 	if entry in target_entries:
# 	    source_fullpath = os.path.join(source_dir, entry)
# 	    target_fullpath = os.path.join(source_dir, entry)
# 	    if os.path.isdir(source_fullpath) and os.path.isdir(target_fullpath):
# 		compare_dir(source_fullpath, target_fullpath)
# 	    else:
# 		compare_file(source_fullpath, target_fullpath)
# 	else:
# 	    print source_fullpath + " -> "
#
#     for entry in target_entries:
# 	if entry not in source_entries:
# 	    target_fullpath = source_dir + "/" + entry
# 	    print " <- " + target_fullpath


def list_all_files(folder, md5sum=False):
    """print all files under a given folder recursively"""
    # os.listdir()
    l = []
    for root, dirs, files in os.walk(folder):
        for file in files:
            full_path = os.path.join(root, file)
            stat_info = os.stat(full_path)
            md5sum_value = ''
            if md5sum:
                md5sum_value = get_file_md5(full_path)
            l.append((full_path,
                      stat_info.st_size,
                      time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(stat_info.st_mtime)),
                      md5sum_value))
    return l


def list_all_files_into_csv(csv_file, folder, md5sum=False):
    file_records = list_all_files(folder, md5sum)
    with open(csv_file, 'w') as fo:
        csv_writer = csv.writer(fo, dialect='excel')
        for r in file_records:
            csv_writer.writerow(r)


def get_file_md5(file):
    m = hashlib.md5()
    with open(file, 'rb') as fo:
        while True:
            data = fo.read(4096)
            if not data:
                break
            m.update(data)
    return m.hexdigest()


def compare_dir_std_strategy(dir1, dir2):
    """compare two directories using the standard strategy"""
    dirobj = filecmp.dircmp(dir1, dir2)
    dirobj.report()


def compare_dir_by_name(dir1, dir2):
    """compare files in two directories by file name no matter where file is"""


def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    print(f'Hi, {name}')  # Press Ctrl+F8 to toggle the breakpoint.


def foo(a, b):
    """asdf"""
    return a + b


def delete_duplicate(primary_folder, secondary_folder, recursive=False):
    """delete duplicate files under secondary folder after comparing folders using dircmp"""
    dirobj = filecmp.dircmp(primary_folder, secondary_folder)
    for file in dirobj.same_files:
        path = os.path.join(dirobj.right, file)
        print('deleting... ' + path)
    # os.remove(path)

    if recursive:
        for common_dir in dirobj.common_dirs:
            delete_duplicate(os.path.join(primary_folder, common_dir),
                             os.path.join(secondary_folder, common_dir),
                             recursive)


# Press the green button in the gutter to run the script.
if __name__ == '__main__':

    # delete_duplicate(r'e:\tmp\vim80', r'e:\tmp\vim82')
    # file_records = list_all_files(r"d:\cosmos\resources\articles", False)
    list_all_files_into_csv(r'e:\cosmos_files.txt', r"d:\cosmos\resources\articles", False)
    # print("start...")
    # dirobj = filecmp.dircmp(r'd:\cosmos\apps\core\vim\vim80', r'd:\cosmos\apps\core\vim\vim82')
    # print(dirobj.common)
    # dirobj.report()
    # dirobj.report_full_closure()

    source_dir = sys.argv[1]
    target_dir = sys.argv[2]

    if not os.path.isdir(source_dir):
        print('source dir "' + source_dir + '" is not a directory!')
    if not os.path.isdir(target_dir):
        print('target dir "' + target_dir + '" is not a directory!')

    # compare_dir(source_dir, target_dir)
