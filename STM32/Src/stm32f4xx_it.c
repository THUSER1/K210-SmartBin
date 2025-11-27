/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file    stm32f4xx_it.c
  * @brief   Interrupt Service Routines.
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2025 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "stm32f4xx_it.h"
/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "stdio.h"
#include "string.h"
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN TD */

/* USER CODE END TD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
#define BUFFER_SIZE  255
#define FRAME_HEADER1  0xAA  // UART1帧头字节1
#define FRAME_HEADER2  0xBB  // UART1帧头字节2
#define FRAME_END      0x5B  // UART1结束标志
#define FRAME2_HEADER  0xCC  // UART2帧头字节
#define FRAME2_END      0xDD  // UART2结束标志
#define UART_RX_BUFFER_SIZE 6
#define UART_RX2_BUFFER_SIZE 4
//uint8_t uart_rx_buffer[6];  // 串口接收缓冲区（6字节: 帧头+数据+结束标志）
//uint8_t uart_rx2_buffer[4];
uint8_t uart_rx_index = 0;  // 当前接收字节的索引
uint8_t receiving = 0;      // 是否正在接收数据
uint8_t rec_flag=0;
uint8_t obj_id=0;
uint8_t turnon=0;
uint8_t change_flag=0;
char ble_message[50];
extern uint8_t uart_rx_dma_buffer[UART_RX_BUFFER_SIZE];
extern uint8_t uart_rx2_dma_buffer[UART_RX2_BUFFER_SIZE];
extern DMA_HandleTypeDef hdma_usart1_rx;
extern UART_HandleTypeDef huart1;
extern UART_HandleTypeDef huart2;
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
/* USER CODE BEGIN PV */

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
/* USER CODE BEGIN PFP */
void THUSER_UART_RxCpltCallback(UART_HandleTypeDef *huart);

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */

// 发送数据至 BLE 模块 (USART2)
void SendDataToBLE(uint8_t *data, uint16_t size) {
    HAL_UART_Transmit(&huart2, data, size, 100);  // 通过 USART2 发送至 BLE
}

/* USER CODE END 0 */

/* External variables --------------------------------------------------------*/
extern TIM_HandleTypeDef htim2;
extern DMA_HandleTypeDef hdma_usart1_rx;
extern DMA_HandleTypeDef hdma_usart2_rx;
extern UART_HandleTypeDef huart1;
extern UART_HandleTypeDef huart2;
/* USER CODE BEGIN EV */

/* USER CODE END EV */

/******************************************************************************/
/*           Cortex-M4 Processor Interruption and Exception Handlers          */
/******************************************************************************/
/**
  * @brief This function handles Non maskable interrupt.
  */
void NMI_Handler(void)
{
  /* USER CODE BEGIN NonMaskableInt_IRQn 0 */

  /* USER CODE END NonMaskableInt_IRQn 0 */
  /* USER CODE BEGIN NonMaskableInt_IRQn 1 */
   while (1)
  {
  }
  /* USER CODE END NonMaskableInt_IRQn 1 */
}

/**
  * @brief This function handles Hard fault interrupt.
  */
void HardFault_Handler(void)
{
  /* USER CODE BEGIN HardFault_IRQn 0 */

  /* USER CODE END HardFault_IRQn 0 */
  while (1)
  {
    /* USER CODE BEGIN W1_HardFault_IRQn 0 */
    /* USER CODE END W1_HardFault_IRQn 0 */
  }
}

/**
  * @brief This function handles Memory management fault.
  */
void MemManage_Handler(void)
{
  /* USER CODE BEGIN MemoryManagement_IRQn 0 */

  /* USER CODE END MemoryManagement_IRQn 0 */
  while (1)
  {
    /* USER CODE BEGIN W1_MemoryManagement_IRQn 0 */
    /* USER CODE END W1_MemoryManagement_IRQn 0 */
  }
}

/**
  * @brief This function handles Pre-fetch fault, memory access fault.
  */
void BusFault_Handler(void)
{
  /* USER CODE BEGIN BusFault_IRQn 0 */

  /* USER CODE END BusFault_IRQn 0 */
  while (1)
  {
    /* USER CODE BEGIN W1_BusFault_IRQn 0 */
    /* USER CODE END W1_BusFault_IRQn 0 */
  }
}

/**
  * @brief This function handles Undefined instruction or illegal state.
  */
void UsageFault_Handler(void)
{
  /* USER CODE BEGIN UsageFault_IRQn 0 */

  /* USER CODE END UsageFault_IRQn 0 */
  while (1)
  {
    /* USER CODE BEGIN W1_UsageFault_IRQn 0 */
    /* USER CODE END W1_UsageFault_IRQn 0 */
  }
}

/**
  * @brief This function handles System service call via SWI instruction.
  */
void SVC_Handler(void)
{
  /* USER CODE BEGIN SVCall_IRQn 0 */

  /* USER CODE END SVCall_IRQn 0 */
  /* USER CODE BEGIN SVCall_IRQn 1 */

  /* USER CODE END SVCall_IRQn 1 */
}

/**
  * @brief This function handles Debug monitor.
  */
void DebugMon_Handler(void)
{
  /* USER CODE BEGIN DebugMonitor_IRQn 0 */

  /* USER CODE END DebugMonitor_IRQn 0 */
  /* USER CODE BEGIN DebugMonitor_IRQn 1 */

  /* USER CODE END DebugMonitor_IRQn 1 */
}

/**
  * @brief This function handles Pendable request for system service.
  */
void PendSV_Handler(void)
{
  /* USER CODE BEGIN PendSV_IRQn 0 */

  /* USER CODE END PendSV_IRQn 0 */
  /* USER CODE BEGIN PendSV_IRQn 1 */

  /* USER CODE END PendSV_IRQn 1 */
}

/**
  * @brief This function handles System tick timer.
  */
void SysTick_Handler(void)
{
  /* USER CODE BEGIN SysTick_IRQn 0 */

  /* USER CODE END SysTick_IRQn 0 */
  HAL_IncTick();
  /* USER CODE BEGIN SysTick_IRQn 1 */

  /* USER CODE END SysTick_IRQn 1 */
}

/******************************************************************************/
/* STM32F4xx Peripheral Interrupt Handlers                                    */
/* Add here the Interrupt Handlers for the used peripherals.                  */
/* For the available peripheral interrupt handler names,                      */
/* please refer to the startup file (startup_stm32f4xx.s).                    */
/******************************************************************************/

/**
  * @brief This function handles DMA1 stream5 global interrupt.
  */
void DMA1_Stream5_IRQHandler(void)
{
  /* USER CODE BEGIN DMA1_Stream5_IRQn 0 */

  /* USER CODE END DMA1_Stream5_IRQn 0 */
  HAL_DMA_IRQHandler(&hdma_usart2_rx);
  /* USER CODE BEGIN DMA1_Stream5_IRQn 1 */

  /* USER CODE END DMA1_Stream5_IRQn 1 */
}

/**
  * @brief This function handles TIM2 global interrupt.
  */
void TIM2_IRQHandler(void)
{
  /* USER CODE BEGIN TIM2_IRQn 0 */

  /* USER CODE END TIM2_IRQn 0 */
  HAL_TIM_IRQHandler(&htim2);
  /* USER CODE BEGIN TIM2_IRQn 1 */

  /* USER CODE END TIM2_IRQn 1 */
}

/**
  * @brief This function handles USART1 global interrupt.
  */
void USART1_IRQHandler(void)
{
  /* USER CODE BEGIN USART1_IRQn 0 */

  /* USER CODE END USART1_IRQn 0 */
  HAL_UART_IRQHandler(&huart1);
  /* USER CODE BEGIN USART1_IRQn 1 */
//THUSER_UART_RxCpltCallback(&huart1);
  /* USER CODE END USART1_IRQn 1 */
}

/**
  * @brief This function handles USART2 global interrupt.
  */
void USART2_IRQHandler(void)
{
  /* USER CODE BEGIN USART2_IRQn 0 */

  /* USER CODE END USART2_IRQn 0 */
  HAL_UART_IRQHandler(&huart2);
  /* USER CODE BEGIN USART2_IRQn 1 */

  /* USER CODE END USART2_IRQn 1 */
}

/**
  * @brief This function handles DMA2 stream2 global interrupt.
  */
void DMA2_Stream2_IRQHandler(void)
{
  /* USER CODE BEGIN DMA2_Stream2_IRQn 0 */

  /* USER CODE END DMA2_Stream2_IRQn 0 */
  HAL_DMA_IRQHandler(&hdma_usart1_rx);
  /* USER CODE BEGIN DMA2_Stream2_IRQn 1 */

  /* USER CODE END DMA2_Stream2_IRQn 1 */
}

/* USER CODE BEGIN 1 */
void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart) {
	printf("1\n");
    if (huart->Instance == USART1) { // 确保是 USART1
			printf("2\n");
        if (uart_rx_dma_buffer[0] == FRAME_HEADER1 &&
            uart_rx_dma_buffer[1] == FRAME_HEADER2 &&
            uart_rx_dma_buffer[5] == FRAME_END) { // 校验帧头 & 结束标志
            int center_x = uart_rx_dma_buffer[2];
            int center_y = uart_rx_dma_buffer[3];
             obj_id = uart_rx_dma_buffer[4];

					/*		            // 格式化数据
            
            sprintf(ble_message, "X=%d, Y=%d, ID=%d\n", center_x, center_y, obj_id);

            // **通过 USART2 发送数据至 BLE**
            SendDataToBLE((uint8_t *)ble_message, strlen(ble_message));*/
						//SendDataToBLE(test, UART_RX_BUFFER_SIZE);       
						SendDataToBLE(uart_rx_dma_buffer, UART_RX_BUFFER_SIZE);   //恢复
            printf("Sent to BLE: %s", ble_message);
							
            printf("Received: x=%d, y=%d, id=%d\n", center_x, center_y, obj_id);
							rec_flag=1;
        }

        // 继续启动 DMA 接收，避免 DMA 停止
        HAL_UART_Receive_DMA(&huart1, uart_rx_dma_buffer, UART_RX_BUFFER_SIZE);
    }
		
		    if (huart->Instance == USART2) { // 确保是 USART1
        if (uart_rx2_dma_buffer[0] == FRAME2_HEADER && uart_rx2_dma_buffer[3] == FRAME2_END) { // 校验帧头 & 结束标志
               if (uart_rx2_dma_buffer[1] == 0x01)
							 {
								 turnon=1;
								 printf("BLE is off");
							 } 
							 else if(uart_rx2_dma_buffer[1] == 0x00)
							 {
								 turnon=0;
								 printf("BLE is on");
								 if(uart_rx2_dma_buffer[2] == 0x01)
								 {
									 obj_id=1;
									 change_flag=1;
									 printf("error1\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x02)
								 {
									 obj_id=2;
									 change_flag=1;
									 printf("error2\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x03)
								 {
									 obj_id=3;
									 change_flag=1;
									 printf("error3\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x04)
								 {
									 obj_id=4;
									 change_flag=1;
									 printf("error4\n");
								 }
							 }
        }

        // 继续启动 DMA 接收，避免 DMA 停止
        HAL_UART_Receive_DMA(&huart2, uart_rx2_dma_buffer, UART_RX2_BUFFER_SIZE);
    }
}

/*void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
	
	if (huart->Instance == USART1) { // 确保是 USART1
        uint8_t received_byte;
        HAL_UART_Receive(&huart1, &received_byte, 1, 100);  // 读取1字节
		    //printf("flag1\n");
		printf("flag1,byte1:%d\n",received_byte);
        if (!receiving) {  // 还未开始接收
            if (received_byte == FRAME_HEADER1) {  // 检测到帧头第1个字节
                uart_rx_buffer[0] = received_byte;
                uart_rx_index = 1;
                receiving = 1;  // 标记开始接收
							printf("flag2,byte1:%d\n",received_byte);
            }
        } else {
            uart_rx_buffer[uart_rx_index] = received_byte;  // 存入缓冲区
            printf("byte%d:%d\n",uart_rx_index,received_byte);
					  uart_rx_index++;
            if (uart_rx_index == 6) {  // 已接收完整6字节
                if (uart_rx_buffer[0] == FRAME_HEADER1 &&
                    uart_rx_buffer[1] == FRAME_HEADER2 &&
                    uart_rx_buffer[5] == FRAME_END) {  // 校验帧头 & 结束标志
                    int center_x = uart_rx_buffer[2];
                    int center_y = uart_rx_buffer[3];
                    int obj_id = uart_rx_buffer[4];

                    // 处理接收到的数据
                    printf("Received: x=%d, y=%d, id=%d\n", center_x, center_y, obj_id);
                }
                receiving = 0;  // 复位接收状态
                uart_rx_index = 0;
            }
        }

        // 重新启动 UART 接收中断，确保继续接收后续数据
        HAL_UART_Receive_IT(&huart1, &received_byte, 1);
    }
}*/

/*void THUSER_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
	printf("flag1\n");
	if (huart->Instance == USART1) { // 确保是 USART1
        uint8_t received_byte;
        HAL_UART_Receive(&huart1, &received_byte, 1, 100);  // 读取1字节
		//printf("flag2,byte1:%d\n",received_byte);
        if (!receiving) {  // 还未开始接收
            if (received_byte == FRAME_HEADER1) {  // 检测到帧头第1个字节
                uart_rx_buffer[0] = received_byte;
                uart_rx_index = 1;
                receiving = 1;  // 标记开始接收
							printf("flag2,byte1:%d\n",received_byte);
            }
        } else {
            uart_rx_buffer[uart_rx_index] = received_byte;  // 存入缓冲区
            printf("byte%d:%d\n",uart_rx_index,received_byte);
					  uart_rx_index++;
            if (uart_rx_index == 6) {  // 已接收完整6字节
                if (uart_rx_buffer[0] == FRAME_HEADER1 &&
                    uart_rx_buffer[1] == FRAME_HEADER2 &&
                    uart_rx_buffer[5] == FRAME_END) {  // 校验帧头 & 结束标志
                    int center_x = uart_rx_buffer[2];
                    int center_y = uart_rx_buffer[3];
                    int obj_id = uart_rx_buffer[4];

                    // 处理接收到的数据
                    printf("Received: x=%d, y=%d, id=%d\n", center_x, center_y, obj_id);
                }
                receiving = 0;  // 复位接收状态
                uart_rx_index = 0;
            }
        }

        // 重新启动 UART 接收中断，确保继续接收后续数据
        HAL_UART_Receive_IT(&huart1, &received_byte, 1);
    }
}*/
/* USER CODE END 1 */
