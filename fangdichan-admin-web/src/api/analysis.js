import request from './request'

export function getVacancyAnalysis() {
  return request.get('/agent/analysis/vacancy').then((res) => res.data)
}
