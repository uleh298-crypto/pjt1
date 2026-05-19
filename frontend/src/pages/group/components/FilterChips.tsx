import React from 'react';

interface FilterChipsProps {
    filters: string[];
    selectedFilter: string;
    onFilterChange: (filter: string) => void;
}

const FilterChips: React.FC<FilterChipsProps> = ({ filters, selectedFilter, onFilterChange }) => {
    return (
        <div style={{
            display: 'flex',
            gap: '6px',
            padding: '8px 20px',
            overflowX: 'auto',
            whiteSpace: 'nowrap'
        }}>
            {filters.map((filter) => {
                const isSelected = selectedFilter === filter;
                return (
                    <button
                        key={filter}
                        onClick={() => onFilterChange(filter)}
                        style={{
                            padding: '6px 14px',
                            fontSize: '12px',
                            borderRadius: '20px',
                            border: isSelected ? 'none' : '1px solid var(--border-color)',
                            backgroundColor: isSelected ? 'var(--primary)' : 'transparent',
                            color: isSelected ? 'white' : 'var(--on-surface)',
                            cursor: 'pointer',
                            fontWeight: isSelected ? '600' : '500',
                            transition: 'all 0.2s',
                            flexShrink: 0
                        }}
                        onMouseEnter={(e) => {
                            if (!isSelected) {
                                e.currentTarget.style.backgroundColor = 'rgba(100, 149, 235, 0.1)';
                            }
                        }}
                        onMouseLeave={(e) => {
                            if (!isSelected) {
                                e.currentTarget.style.backgroundColor = 'transparent';
                            }
                        }}
                    >
                        {filter}
                    </button>
                );
            })}
        </div>
    );
};

export default FilterChips;
