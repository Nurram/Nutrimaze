    package com.myapps.pacman.utils.matrix

    fun interface MatrixTraverseDo<T,R> {
        fun matrixDo(element: T?,rowIndex:Int,columnIndex:Int,contextVariable:R?)
    }