package com.und.service

import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.BeforeClass
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

@RunWith(Parameterized::class)
class EventSegmentProcessingTest {

    private  var input:String
    private  var output:String

    constructor(_input:String,_output:String){
        input = _input
        output = _output
    }

    constructor():this("",""){
    }


    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeClass(){
            println("Before class")
        }

        @JvmStatic
        @Parameterized.Parameters
        fun parameters():List<Array<String>>{
            var data:Array<Array<String>> = arrayOf(arrayOf<String>("hello","hello"), arrayOf<String>("welcome","welcome"))
            return data.asList()
        }
    }

    @Test(expected = RuntimeException::class)
    fun test1(){
        assertEquals("match",input,output)
        var r = mock(EventSegmentProcessing::class.java)
        var list = mock(List::class.java)
        `when`(list.size).thenReturn(2).thenReturn(3)
        //`when`(list.get(anyInt())).thenReturn(0)
        `when`(list.get(anyInt())).thenThrow(RuntimeException("exception"))
        assertEquals(list.size,2)
        assertEquals(list.size,3)

        //this will check is this subList method is called or not.
        //verify(list).subList(0,2)
        //this will check that indexof method is never called
        verify(list, never()).indexOf(2)
        list.get(0)
        verify(list, times(1)).get(0)

        //if we want to know on which argument a particular method is called

        var intArgumentCapture:ArgumentCaptor<Int> = ArgumentCaptor.forClass(Int::class.java)
        list.listIterator(90)
        verify(list).listIterator(intArgumentCapture.capture())
        print(intArgumentCapture.value)
        assertThat(intArgumentCapture.value, Matchers.`is`(89))


    }

}