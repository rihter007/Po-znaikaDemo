package ru.po_znaika.alphabet;

import android.support.annotation.NonNull;

/**
 * Created by Rihter on 11.03.2015.
 * Allowes to access array of elements as matrix
 */
public class MatrixAccessor<T>
{
    public MatrixAccessor(@NonNull T[] _elements, int columnsCount)
    {
        m_elements = _elements;
        m_columnsCount = columnsCount;
    }

    public void set(int rowIndex, int columnIndex, T value)
    {
        final int arrayIndex = rowIndex * m_columnsCount + columnIndex;
        m_elements[arrayIndex] = value;
    }

    public T get(int rowIndex, int columnIndex)
    {
        final int arrayIndex = rowIndex * m_columnsCount + columnIndex;
        if (arrayIndex < m_elements.length)
            return m_elements[arrayIndex];
        return null;
    }

    public int getRowsCount()
    {
        return(int) Math.floor((double)m_elements.length / m_columnsCount);
    }

    public int getColumnsCount()
    {
        return m_columnsCount;
    }

    public int getElementsCount()
    {
        return m_elements.length;
    }

    public T[] get()
    {
        return m_elements;
    }

    private T[] m_elements;
    private int m_columnsCount;
}
