package com.myapps.pacman.utils.matrix

class Matrix<T>(private var rows:Int, private var columns:Int) {

    private var matrix:MutableList<T?>?

    init {
        matrix = if(rows >= 0 && columns >= 0){
            val totalSize = rows*columns
            MutableList(totalSize){null}
        } else{
            null
        }
    }
    fun getRows():Int = rows

    fun getColumns():Int = columns

    fun insertElement(element:T,rowIndex:Int,columnIndex:Int){
        if(rowIndex in 0..<rows &&  columnIndex in 0..<columns){
            val index = rowIndex * columns + columnIndex
            matrix?.set(index, element)
        }
    }

    fun getElementByPosition(rowIndex: Int,columnIndex: Int):T?{
        if(rowIndex in 0..<rows  && columnIndex in 0..<columns){
            val index = rowIndex * columns+ columnIndex
            return matrix?.get(index)
        }
        return null
    }

    fun addRow(){
        rows += 1
        val newSizeArray = rows*columns
        val array = MutableList<T?>(newSizeArray){null}
        System.arraycopy(matrix as MutableList,0,array,0,newSizeArray-columns)
        matrix = array
    }

    fun addColumn(){
        columns += 1
        val newSizeArray = rows*columns
        val array = MutableList<T?>(newSizeArray){null}
        System.arraycopy(matrix as MutableList,0,array,0,newSizeArray-rows)
        matrix = array

        for (i in rows - 1 downTo 0) {
            val index = columns * (i + 1) - 1

            for (j in index - 1 downTo index - columns + 1) {
                matrix?.set(i, matrix!![j-1])
            }
        }
    }

    fun deleteRow(rowIndex:Int):Boolean{
        if (rowIndex < 0 || rowIndex >= rows) return false

        val initialIndex: Int = columns * (rowIndex + 1)
        val totalCapacity = rows * columns

        for (i in initialIndex until totalCapacity) {
            matrix?.set(i-columns, matrix!![i])
        }

        rows -= 1
        val array = MutableList<T?>(totalCapacity - columns){null}
        System.arraycopy(matrix as MutableList, 0, array, 0, totalCapacity - columns)
        matrix = array

        return true
    }

    fun deleteColumn(columnIndex: Int):Boolean{
        if (columnIndex < 0 || columnIndex >= columns) return false

        for (i in 0 until rows) {
            var index: Int = i * (columns + columnIndex) + 1
            while (index < index + columns && index < rows * columns) {
                matrix?.set(index - (i+1), matrix!![index])
                index++
            }
        }

        columns -= 1
        val newSize = rows * columns
        val array = MutableList<T?>(newSize){null}
        System.arraycopy(matrix as MutableList, 0, array, 0, newSize)

        return true
    }


    fun copy(): Matrix<T> {
        val newMatrix = Matrix<T>(this.rows, this.columns)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                val element = getElementByPosition(i, j)
                if (element != null) {
                    newMatrix.insertElement(element, i, j)
                }
            }
        }
        return newMatrix
    }

    fun <R> traverse(matrixDo: MatrixTraverseDo<T, R>, contextVariable:R ?= null){
        for(i in 0..<rows){
            for(j in 0..<columns){

                matrixDo.matrixDo(this.getElementByPosition(i,j),i,j,contextVariable)

            }
        }
    }

}