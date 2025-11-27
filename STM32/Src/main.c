/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
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
#include "dma.h"
#include "tim.h"
#include "usart.h"
#include "gpio.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "stdio.h"
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */

/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/

/* USER CODE BEGIN PV */
#define UART_RX_BUFFER_SIZE 6   // 定义接收数据长度
#define UART_RX2_BUFFER_SIZE 4 
uint8_t test[6]={0xAA,0xBB,0x01,0x02,0x01,0x5B};
uint8_t test1[6]={0xAA,0xBB,0x75,0x02,0x02,0x5B};
uint8_t test2[6]={0xAA,0xBB,0x01,0x22,0x03,0x5B};
uint8_t test3[6]={0xAA,0xBB,0xAB,0x43,0x04,0x5B};
uint8_t uart_rx_dma_buffer[UART_RX_BUFFER_SIZE];  // DMA 接收缓冲区
uint8_t uart_rx2_dma_buffer[UART_RX2_BUFFER_SIZE];
extern uint8_t rec_flag;
extern uint8_t obj_id;
extern uint8_t turnon;
extern uint8_t change_flag;
/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
/* USER CODE BEGIN PFP */

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */

void Servo_SetAngle270(TIM_HandleTypeDef *htim, uint32_t channel, uint8_t angle, uint8_t speed)
{
    if (angle > 270)
		{
			angle = 270%360;
		}
    // 计算目标脉宽（单位：us）
    uint16_t pulse_result = 500 + (angle * 2000) / 270;

    // 获取当前脉宽
    uint16_t pulse_temp = __HAL_TIM_GET_COMPARE(htim, channel);

    while (abs(pulse_temp - pulse_result) > speed)
    {
        if (pulse_temp < pulse_result)
        {
            pulse_temp += speed;
        }
        else
        {
            pulse_temp -= speed;
        }

        __HAL_TIM_SET_COMPARE(htim, channel, pulse_temp);
        HAL_Delay(50); // 越小越快，可调
    }

    // 最后精确到目标值
    __HAL_TIM_SET_COMPARE(htim, channel, pulse_result);
}

void HAL_TIM_PeriodElapsedCallback(TIM_HandleTypeDef *htim)
	{
	
		 if(htim->Instance == TIM2)      //50Hz
	  {
			//printf("in tim2!\n");
       if(change_flag==1)
			 {
				 printf("change!\n");
				 change_flag=0;
				 Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 135, 20);  //检测错误后，立刻归位，重新进入main函数选择更改后的投放方式
				 Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 135, 20);
				 Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 135, 20);
				 printf("识别纠正！\n");
								 if(uart_rx2_dma_buffer[2] == 0x01)
								 {
									 printf("应为纸板！\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x02)
								 {
									 printf("应为废纸！\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x03)
								 {
									 printf("应为瓶子！\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x04)
								 {
									 printf("应为电池！\n");
								 }				 
			 }
		}
		
	}
	



/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int main(void)
{

  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */

  /* MCU Configuration--------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_DMA_Init();
  MX_USART1_UART_Init();
  MX_USART2_UART_Init();
  MX_TIM4_Init();
  MX_TIM2_Init();
  /* USER CODE BEGIN 2 */
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_1, 1500); 
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_2, 1500); 
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_3, 1500); 
	HAL_TIM_PWM_Start(&htim4, TIM_CHANNEL_1);
	HAL_TIM_PWM_Start(&htim4, TIM_CHANNEL_2);
	HAL_TIM_PWM_Start(&htim4, TIM_CHANNEL_3);
	//HAL_TIM_Base_Start_IT(&htim2);
	/*Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 20, 20);
	HAL_Delay(2000);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
	HAL_Delay(2000);*/
	/*Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 60, 20);
	HAL_Delay(2000);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 80, 20);
	HAL_Delay(2000);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 100, 20);
	HAL_Delay(2000);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 120, 20);
		HAL_Delay(2000);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 140, 20);
	HAL_Delay(2000);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 160, 20);*/
		/*Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 255, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 120, 20);
		HAL_Delay(2000);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 50, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 165, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
		HAL_Delay(2000);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 135, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 135, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 135, 20);*/
	/*Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 135, 20);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 135, 20);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 135, 20);

	HAL_Delay(500);
	
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 100, 20);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 100, 20);
	Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 100, 20);*/
/*HAL_TIM_PWM_Start(&htim4, TIM_CHANNEL_1);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_1, 1800); // 中位
HAL_Delay(2000);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_1, 500);  // 最左
HAL_Delay(2000);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_1, 2500); // 最右
HAL_Delay(2000);*/

/*HAL_TIM_PWM_Start(&htim4, TIM_CHANNEL_2);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_2, 2000); // 中位
HAL_Delay(2000);*/
/*__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_2, 500);  // 最左
HAL_Delay(2000);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_2, 2500); // 最右
HAL_Delay(2000);*/

/*HAL_TIM_PWM_Start(&htim4, TIM_CHANNEL_3);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_3, 2200); // 中位
HAL_Delay(2000);*/
/*__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_3, 500);  // 最左
HAL_Delay(2000);
__HAL_TIM_SET_COMPARE(&htim4, TIM_CHANNEL_3, 2500); // 最右
HAL_Delay(2000);
*/
//uint8_t temp;
//HAL_UART_Receive_IT(&huart1, &temp, 1);  // 启动 UART 中断接收
HAL_UART_Receive_DMA(&huart1, uart_rx_dma_buffer, UART_RX_BUFFER_SIZE);
HAL_UART_Receive_DMA(&huart2, uart_rx2_dma_buffer, UART_RX2_BUFFER_SIZE);

  /* USER CODE END 2 */

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  while (1)
  {
    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
//printf("1");
		if(change_flag==1)
			 {
				 printf("change!\n");
				 change_flag=0;
				 Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 135, 20);  //检测错误后，立刻归位，重新进入main函数选择更改后的投放方式
				 Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 135, 20);
				 Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 135, 20);
				 printf("识别纠正！\n");
								 if(uart_rx2_dma_buffer[2] == 0x01)
								 {
									 printf("应为纸板！\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x02)
								 {
									 printf("应为废纸！\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x03)
								 {
									 printf("应为瓶子！\n");
								 }
								 else if(uart_rx2_dma_buffer[2] == 0x04)
								 {
									 printf("应为电池！\n");
								 }		
							rec_flag=1;						 
			 }
		if(rec_flag==1&&turnon==0)
		{
			if(obj_id==1)//纸板
			{
		Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);// 爪子张开
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 255, 20);  //向左倾斜
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 150, 20);  //爪子闭合
		HAL_Delay(500);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 50, 20);  //向右倾斜
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 155, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);  //爪子放开
			}
			else if(obj_id==2)//废纸
			{
		Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 255, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 150, 20);
		HAL_Delay(500);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 50, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 195, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
			}
			else if(obj_id==3)//瓶子
			{
		Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 255, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 120, 20);
		HAL_Delay(500);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 50, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 105, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
			}
			else if(obj_id==4)//电池
			{
		Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 255, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 160, 20);
		HAL_Delay(500);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 50, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 105, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 40, 20);
			}
		HAL_Delay(500);
		Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 135, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 135, 20);
	  Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 135, 20);
			rec_flag=0;
		}

		//printf("sdfsdfsdfsdfxcv\n");
		//HAL_Delay(1000);
		/*SendDataToBLE(test, UART_RX_BUFFER_SIZE);
		HAL_Delay(1500);
		SendDataToBLE(test1, UART_RX_BUFFER_SIZE);
		HAL_Delay(1500);
		SendDataToBLE(test2, UART_RX_BUFFER_SIZE);
		HAL_Delay(1500);
		SendDataToBLE(test3, UART_RX_BUFFER_SIZE);
		HAL_Delay(1500);*/
		// 舵机1设置到45°
/*Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 90);

// 舵机2设置到180°
Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 90);

// 舵机3设置到270°（最大值）
Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 90);
HAL_Delay(3000);
Servo_SetAngle270(&htim4, TIM_CHANNEL_1, 250);

// 舵机2设置到180°
Servo_SetAngle270(&htim4, TIM_CHANNEL_2, 250);

// 舵机3设置到270°（最大值）
Servo_SetAngle270(&htim4, TIM_CHANNEL_3, 250);*/
/*__HAL_TIM_SetCompare(&htim4,TIM_CHANNEL_1,speed);
__HAL_TIM_SetCompare(&htim4,TIM_CHANNEL_2,speed);
__HAL_TIM_SetCompare(&htim4,TIM_CHANNEL_3,speed);
speed=speed+10;
HAL_Delay(1000);*/
  }
  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};

  /** Configure the main internal regulator output voltage
  */
  __HAL_RCC_PWR_CLK_ENABLE();
  __HAL_PWR_VOLTAGESCALING_CONFIG(PWR_REGULATOR_VOLTAGE_SCALE1);

  /** Initializes the RCC Oscillators according to the specified parameters
  * in the RCC_OscInitTypeDef structure.
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSE;
  RCC_OscInitStruct.HSEState = RCC_HSE_ON;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_ON;
  RCC_OscInitStruct.PLL.PLLSource = RCC_PLLSOURCE_HSE;
  RCC_OscInitStruct.PLL.PLLM = 4;
  RCC_OscInitStruct.PLL.PLLN = 168;
  RCC_OscInitStruct.PLL.PLLP = RCC_PLLP_DIV2;
  RCC_OscInitStruct.PLL.PLLQ = 4;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }

  /** Initializes the CPU, AHB and APB buses clocks
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_PLLCLK;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV4;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV2;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_5) != HAL_OK)
  {
    Error_Handler();
  }
}

/* USER CODE BEGIN 4 */
int fputc(int ch,FILE *f)
{
	 HAL_UART_Transmit(&huart1,(uint8_t*)&ch,1,HAL_MAX_DELAY);
return ch;	
}
int fgetc(FILE*f)
{	
	uint8_t ch;
	HAL_UART_Receive(&huart1,(uint8_t*)&ch,1,HAL_MAX_DELAY);
	return ch;	
}
/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */
  __disable_irq();
  while (1)
  {
  }
  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */
