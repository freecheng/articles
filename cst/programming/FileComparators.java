package fc.file.comparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileComparators {
	public void mockUpUserActions() {
		FileSorter sorter = new FileSorter();
		// User UI Actions on table header:
		//	- single click on one column header
		//		- on the current sorting column	- just reverse sorting
		//			- if previous sorting is multi-key sorting
		//		- on the other column	- re-sort by new column
		sorter.reverseOrTotallyNewSortByKey(1);
		//	- ctrl + single click on one column header
		//		- on the one of current sorting columns	- reverse sorting
		//			- on the last sorting column
		//			- on the first or middle sorting column
		//		- on the other column	- add new sorting column
		sorter.addOrReverseSortByKey(2);
		//	- shift + ctrl + single click on one column header
		//		- on the one of current sorting columns	- remove the sorting column
		//		- on the other column	- add new sorting column
		sorter.addOrRemoveSortByKey(3);
		
		// User UI Actions to pop up a sorting dialog to choose sorting criterias:
		
	}
	

	public static class FileSorter {
		private List<SortCriteria> criterias = new ArrayList<SortCriteria>();
		
		/**
		 * Reverse sorting or totally new sorting on a single Key.
		 * 	If multiple Keys are selected in sorting,
		 * 		[Scenario 1] if the given Key is one of them,
		 * 			reverse the order flag and re-sort.
		 * 		[Scenario 2] else 
		 * 			clear it, and sort by new Key with default order.
		 * 	else
		 * 		[Scenario 3] if the given Key is the selected column,
		 * 			just reverse sort.			
		 * 		[Scenario 4] else
		 * 			re-sort by the given Key.
		 * Typical user ui action: single click on one Key header
		 * @param key
		 */
		public void reverseOrTotallyNewSortByKey(int key) {
			if (isMultiKeySorting()) {
				SortCriteria criteria = find(key);
				if (criteria != null) {
					boolean orderFlag = criteria.isAscending();
					clearSortingCriteria();
					criteria = addNewSortingCriteria(key);
					criteria.setAscending(!orderFlag);
					sort();
				} else {
					clearSortingCriteria();
					addNewSortingCriteria(key);
					sort();
				}
			} else {
				SortCriteria criteria = criterias.get(0);
				if (criteria.getKey() != key) {
					clearSortingCriteria();
					addNewSortingCriteria(key);
					sort();
				} else {
					optimized_reverse_sort();
				}
			}
		}
		
		/**
		 * Add or remove the given Key in multi-key sorting mode.
		 * 	If the given on one of current sorting Keys
		 * 		remove the sorting Key 
		 * 		[Scenario 1] if no selected Key is left, 
		 * 			add default Key and re-sort.
		 * 		[Scenario 2] else  
		 * 			re-sort.
		 * 	else [Scenario 3] 
		 * 		add the given Key and re-sort.
		 * Typical user ui action: shift + ctrl + single click on one Key header
		 * @param key
		 */
		public void addOrRemoveSortByKey(int key) {
			SortCriteria criteria = find(key);
			if (criteria != null) {
				removeSortingCriteria(key);
				sort();
			} else {
				addNewSortingCriteria(key);
				sort();
			}
		}

		/**
		 * Add or reverse the given Key in multi-key sorting mode.
		 * 	[Scenario 1] If the given on one of current sorting Keys
		 * 		reverse the order flag and re-sort. 
		 * 	[Scenario 2] else 
		 * 		add the given Key and re-sort.
		 * Typical user ui action: ctrl + single click on one Key header
		 * @param key
		 */
		public void addOrReverseSortByKey(int key) {
			SortCriteria criteria = find(key);
			if (criteria != null) {
				boolean orderFlag = criteria.isAscending();
				criteria.setAscending(!orderFlag); 	// switch order of the sorting Key
				sort();
			} else {
				addNewSortingCriteria(key);
				sort();
			}
		}
		
		private boolean isMultiKeySorting() {
			return criterias.size() > 1;
		}
		
		private SortCriteria addNewSortingCriteria(int key) {
			SortCriteria c = new SortCriteria(key);
			criterias.add(c);
			return c;
		}

		private void removeSortingCriteria(int key) {
			for (SortCriteria c : criterias) {
				if (c.getKey() == key) {
					criterias.remove(c);
				}
			}
		}

		private void clearSortingCriteria() {
			criterias.clear();
		}
		
		private SortCriteria find(int key) {
			for (SortCriteria c : criterias) {
				if (c.getKey() == key) {
					return c;
				}
			}
			return null;
		}

		private void optimized_reverse_sort() {
			// TODO Auto-generated method stub
			
		}

		private void sort() {
			// TODO Auto-generated method stub
			
		}

	}

	
	public static class SortCriteria {
		private int key = -1;	// 0 - name; ...
		private boolean ascending = true;	
		private boolean caseSensitive = false;
		
		public SortCriteria(int key) {
			this.key = key;
		}
		
		public SortCriteria(int key, boolean ascending, boolean caseSensitive) {
			this.key = key;
			this.ascending = ascending;
			this.caseSensitive = caseSensitive;
		}
		
		public int getKey() {
			return key;
		}
		public void setKey(int key) {
			this.key = key;
		}
		public boolean isAscending() {
			return ascending;
		}
		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		}
		public boolean isCaseSensitive() {
			return caseSensitive;
		}
		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
		}
	}
	
	public static class FileNameComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			// TODO:
			return 0;
		}
	}
	
	public static class FileSizeComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			return (int)(f1.length() - f2.length());
		}
	}
	
	public static class FileTimeComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			return (int)(f1.lastModified() - f2.lastModified());
		}
	}
	
	public static class FilePermissionComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			return (int)(f1.lastModified() - f2.lastModified());
		}
	}
	

    /**
     * Null-friendly comparators
     */
	public final static class NullComparator<T> implements Comparator<T> {
        private final boolean nullFirst;
        // if null, non-null Ts are considered equal
        private final Comparator<T> realComparator;

        @SuppressWarnings("unchecked")
        public NullComparator(boolean nullFirst, Comparator<? super T> real) {
            this.nullFirst = nullFirst;
            this.realComparator = (Comparator<T>) real;
        }

        @Override
        public int compare(T a, T b) {
            if (a == null) {
                return (b == null) ? 0 : (nullFirst ? -1 : 1);
            } else if (b == null) {
                return nullFirst ? 1: -1;
            } else {
                return (realComparator == null) ? 0 : realComparator.compare(a, b);
            }
        }
        
         
    }
    
    
    public final static class BooleanComparator implements Comparator<Boolean> {
        private final boolean falseFirst;

        public BooleanComparator(boolean falseFirst) {
            this.falseFirst = falseFirst;
        }

        @Override
        public int compare(Boolean b1, Boolean b2) {
        	if (b1 == false && b2 == true) {
    			return falseFirst ? -1 : 1;
        	} else if (b1 == true && b2 == false) {
    			return falseFirst ? 1 : -1;
        	} else {
        		return 0;	// both are false or both are true
        	}
        }
    }
    
    public static class StringComparator implements Comparator<String> {
    	private boolean caseSensistive = false;
    	
		public StringComparator() {
    	}
    	
    	public StringComparator(boolean caseSensistive) {
    		this.caseSensistive = caseSensistive;
    	}
    	
    	public boolean isCaseSensistive() {
			return caseSensistive;
		}

        @Override
        public int compare(String s1, String s2) {
        	return caseSensistive ? s1.compareTo(s2) : s1.compareToIgnoreCase(s2);
        }
    }

    public static class NatrualOrderStringComparator extends StringComparator {
    	public NatrualOrderStringComparator() {
    	}
    	
    	public NatrualOrderStringComparator(boolean caseSensistive) {
    		super(caseSensistive);
    	}
    	
        @Override
        public int compare(String s1, String s2) {
        	// TODO:
        	return super.compare(s1, s2);
        }
    }

    public static class CompositeComparator<T> implements Comparator<T> {
        private List<Comparator<T>> comparators;

        public CompositeComparator(List<Comparator<T>> comparators) {
            this.comparators = comparators;
        }

        @Override
        public int compare(T o1, T o2) {
            for (Comparator<T> comparator : comparators) {
                int comparison = comparator.compare(o1, o2);
                if (comparison != 0) {
                	return comparison;
                }
            }
            return 0;
        }
    }
}
