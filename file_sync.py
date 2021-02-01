#!/usr/bin/env python
import os
import filecmp
import datetime
import time
import hashlib
import pathlib
import csv
import shutil
import sys
import re
from pathlib import Path

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

# compare two files
# - shadow way: by file mode, size, and modified time
# - md5 way: if md5 is not same, must be different; if md5 is same, should compare content byte by byte
# - final way: by file content

# compare two folders
# - in regular file system supported by operating system
# - one in regular file system, one in zip file
# - one in regular file system, one is customized virtual system

# synchronize two folders
# - copy files existing in one folder to another folder in bidirectional way.
# - overwrite common files with different strategies
#   - newer strategy
#   - manual confirm

# delete duplicate files
# - delete duplicate files that are exactly same.
# - delete common files with different strategies
#   - older strategy
#   - manual confirm

# backup/archive a folder
# * full backup strategy
# - delete backup folder and copy all
# * incremental backup strategy
# - copy new files to the backup folder
# - overwrite common files with different strategies with primary folder
# - delete file/sub-folder that exists in primary folder


def get_backup_date(p):
    """get backup date from file name in 20210130 or 2021-01-30 form or from modified time"""
    if re.search(r'\d{8}', p.name):
        return re.search(r'\d{8}', p.name).group(0)
    elif re.search(r'\d{4}-\d{2}-\d{2}', p.name):
        return re.search(r'\d{4}-\d{2}-\d{2}', p.name).group(0).replace('-', '')
    else:
        return datetime.datetime.fromtimestamp(p.stat().st_mtime).strftime('%Y%m%d')


def get_latest_modified_file(source_folder):
    files_in_time_order = sorted(source_folder.glob('**/*'), key=lambda x: x.stat().st_mtime)
    return files_in_time_order[-1]


def get_backup_zip_files(backup_folder, name):
    backup_zip_files = list(backup_folder.glob('*' + name + '*'))
    backup_zip_files.sort(key=get_backup_date, reverse=True)
    return backup_zip_files


def newer_than_backup_zip_file(source_folder, latest_zip_file):
    f1 = get_latest_modified_file(source_folder)
    print(str(f1) + "\t" + str(latest_zip_file))
    return f1.stat().st_mtime > latest_zip_file.stat().st_mtime


def delete_old_backup_zip_file(backup_zip_files, limit):
    if len(backup_zip_files) >= limit:
        for zf in backup_zip_files[2:]:
            print('deleting... ' + zf)
            # zf.unlink()


# get previous backup zip files in a list ordered by time stamp
# if nothing newer than previous backup, skip it
# if no previous backup, just make a new one
# if more than 3 previous backups, remove the oldest one
# TODO if a backup is done the same day
def backup_folder_to_zip_file(source_folder, backup_folder):
    need_to_make_a_backup = False
    file_name = source_folder.name
    backup_zip_files = get_backup_zip_files(backup_folder, file_name)
    print(backup_zip_files)
    if backup_zip_files:
        last_backup_date = get_backup_date(backup_zip_files[0])
        newest_file = get_backup_date(get_latest_modified_file(source_folder))
        print('newest file:' + newest_file + " last_backup_date:" + last_backup_date)
        if newest_file > last_backup_date:
            delete_old_backup_zip_file(backup_zip_files, 3)
            need_to_make_a_backup = True
    else:
        need_to_make_a_backup = True

    if need_to_make_a_backup:
        file_name += datetime.datetime.now().strftime('%Y-%m-%d')
        print('making a backup zip file ' + str(backup_folder.joinpath(file_name)) + '.zip')
        # shutil.make_archive(backup_folder.joinpath(file_name), 'zip', source_folder)


# list(Path('/usr/').glob('**/*.py'))
def tree(directory):
    print(f'- {directory}')
    for path in sorted(directory.rglob('*')):
        depth = len(path.relative_to(directory).parts)
        spacer = '    ' * depth
        print(f'{spacer}+ {path.name}')


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


class FileRecord:
    def __init__(self, path, size, mtime, md5):
        self.path = path
        self.size = size
        self.mtime = mtime
        self.md5 = md5


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
            l.append(FileRecord(full_path,
                      stat_info.st_size,
                      time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(stat_info.st_mtime)),
                      md5sum_value))
    return l


def list_all_files_into_csv(csv_file, folder, md5sum=False):
    file_records = list_all_files(folder, md5sum)
    with open(csv_file, 'w') as fo:
        csv_writer = csv.writer(fo, dialect='excel')
        for r in file_records:
            pass
            # csv_writer.writerow(r)


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


def delete_duplicate_in(folders):
    """calculate md5 for all files, and create a map with md5 as key"""
    m = {}
    for folder in folders:
        file_records = list_all_files(folder, True)
        for file_record in file_records:
            m.setdefault(file_record.md5, []).append(file_record)

    for duplicate_files in m.values():
        if len(duplicate_files) > 1:
            if all(x.size == duplicate_files[0].size for x in duplicate_files) \
                    and all(x.mtime == duplicate_files[0].mtime for x in duplicate_files):
                print('just keep ' + str(duplicate_files[0]) + ' and delete ' + str(duplicate_files[1:]))
                for f in duplicate_files[1:]:
                    try:
                        os.remove(f[0])
                    except Exception as e:
                        print(e)
            else:
                print(duplicate_files)

    for folder in folders:
        delete_empty_folders(folder)


def delete_empty_folders(folder):
    for root, dirs, files in os.walk(folder):
        if not os.listdir(root):
            print('delete empty dir: ' + root)
            os.rmdir(root)


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    pass
